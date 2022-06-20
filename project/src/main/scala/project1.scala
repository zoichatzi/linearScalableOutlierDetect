import org.apache.spark.ml._
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.feature.{MinMaxScaler, VectorAssembler}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{udf, _}


object project1 {

  def main(args: Array[String]): Unit = {

    val sparkSession = SparkSession.builder().appName("Project1").master("local[*]").getOrCreate()

    sparkSession.sparkContext.setLogLevel("ERROR")
    import sparkSession.implicits._ // For implicit conversions like converting RDDs to DataFrames

    // Start time
    val t1 = System.nanoTime

    // Read sample dataset
    val sampleDF = sparkSession.read
      .option("header", "false")
      .option("inferSchema", "true")
      .csv("/home/ozzy/Documents/data-example2122-pms.txt") //"/home/ozzy/Documents/data-example2122-pms.txt"

    // Drop from dataframe the na
    val cleanDF = sampleDF.na.drop()

    // Create features vector
    val assembler = new VectorAssembler()
      .setInputCols(Array("_c0", "_c1"))
      .setOutputCol("vectors")

    val vectorizedDF = assembler.transform(cleanDF)

    val scaler = new MinMaxScaler()
      .setInputCol("vectors")
      .setOutputCol("features")

    // Compute summary statistics and generate MinMaxScalerModel
    val scalerModel = scaler.fit(vectorizedDF)

    // Rescale each feature to range [0, 1]
    val scaledDF = scalerModel.transform(vectorizedDF).select("features")

    // Train a k-means model
    val kmeans = new KMeans()
      .setK(70)
      .setMaxIter(10)

    val model = kmeans.fit(scaledDF)

    // Make predictions
    val predictedDF = model.transform(scaledDF)

    val sizesDF = model.summary.clusterSizes.zipWithIndex.filter( _._1 < 5).map(_._2)

    import org.apache.spark.sql.functions.col

    val microClustersDF = predictedDF.filter(col("prediction").isin(sizesDF: _*))

    // A UDF to convert VectorUDT to ArrayType
    val vecToArray = udf((xs: linalg.Vector) => xs.toArray)

    val unscaleValue = udf((scaled: Double, minVal: Double, maxVal: Double) => (scaled * (maxVal - minVal)) + minVal)

    if (microClustersDF.count() > 0) {

      // Add a ArrayType Column
      val featuresDF = microClustersDF.withColumn("featuresArr", vecToArray($"features")).select("featuresArr")

      // Reverse minmax scaling and print outliers in console
      featuresDF
        .withColumn("x",
          round(unscaleValue($"featuresArr".getItem(0),
            lit(scalerModel.originalMin.apply(0)),
            lit(scalerModel.originalMax.apply(0))), 3))
        .withColumn("y",
          round(unscaleValue($"featuresArr".getItem(1),
            lit(scalerModel.originalMin.apply(1)),
            lit(scalerModel.originalMax.apply(1))), 3))
        .select("x", "y")
        .show()
    }

    val mcIds = microClustersDF.select("prediction").collect.map(_.getInt(0))

    import org.apache.spark.ml.linalg.{Vector, Vectors}

    // UDF that calculates for each point distance from each cluster center
    val distFromCenter = udf((features: Vector, c: Int) => Math.sqrt(Vectors.sqdist(features, model.clusterCenters(c))))

    // Calculate the distances
    val distancesDF = predictedDF.withColumn("distanceFromCenter", distFromCenter($"features", $"prediction"))

    // For each cluster calculate mean and std of its distances
    val metricsDF = distancesDF.groupBy($"prediction").agg(avg($"distanceFromCenter"), stddev($"distanceFromCenter"))
    val arrayFor = metricsDF.select($"prediction").collect.map(_.getInt(0)).diff(mcIds)
    val avgFor = metricsDF.select($"avg(distanceFromCenter)").collect.map(_.getDouble(0))
    val stdevFor = metricsDF.select($"stddev_samp(distanceFromCenter)").collect.map(_.getDouble(0))

    var counter = 0

    // Find the outliers of each cluster (if any) using z-score
    for (one_cluster <- arrayFor) {

      val pointsDF = distancesDF.filter($"prediction" === one_cluster)
      val zscoreDF = pointsDF.withColumn("zscore", abs((col("distanceFromCenter") - avgFor(counter)) / stdevFor(counter)))
      val outliersDF = zscoreDF.filter(col("zscore") >= 4)

      if (outliersDF.count() > 0) {

        // A UDF to convert VectorUDT to ArrayType
        //val vecToArray = udf((xs: linalg.Vector) => xs.toArray)

        // Add a ArrayType Column
        val featDF = outliersDF.withColumn("featuresArr", vecToArray($"features")).select("featuresArr")

        // Reverse minmax scaling and print outliers in console
        featDF
          .withColumn("x",
            round(unscaleValue($"featuresArr".getItem(0),
              lit(scalerModel.originalMin.apply(0)),
              lit(scalerModel.originalMax.apply(0))), 3))
          .withColumn("y",
            round(unscaleValue($"featuresArr".getItem(1),
              lit(scalerModel.originalMin.apply(1)),
              lit(scalerModel.originalMax.apply(1))), 3))
          .select("x", "y")
          .show()

      }
      counter = counter + 1
    }

    print("Execution time in seconds: ")
    print((System.nanoTime - t1) / 1e9d)

  }
}
