# Scalable Outlier Detection in Spark

## Authors

- Maria Christina Maniou [@mcmaniou](https://github.com/mcmaniou)
- Zoi Chatzichristodoulou [@zoichatzi](https://github.com/zoichatzi)

## Table of Contents

[0. Introduction](https://github.com/zoichatzi/linearScalableOutlierDetect#0-introduction)

[1. The Problem](https://github.com/zoichatzi/linearScalableOutlierDetect#1-the-problem)

[2. The Data](https://github.com/zoichatzi/linearScalableOutlierDetect#2-the-data)

[3. About Our Approach](https://github.com/zoichatzi/linearScalableOutlierDetect#3-about-our-approach)

[4. Performance](https://github.com/zoichatzi/linearScalableOutlierDetect#4-performance)

[5. References](https://github.com/zoichatzi/linearScalableOutlierDetect#5-references)


## 0. Introduction

The goal of the present work is to discover abnormalities in two-dimensional data using linear complexity clustering techniques as a foundation. The solution performs effectively and effi-ciently in datasets with well-formed cycloid clusters. The solution has been implemented in Apache Spark using Scala.

*The present work is a project that was created in the context of “MINING FROM MASSIVE DATASETS” class.*

**MSc Data and Web Science**, School of Informatics, Aristotle University of Thessaloniki.



## 1. The Problem



## 2. The Data



## 3. About Our Approach



## 4. Performance

Comparison between all approaches.

| Dataset	                | # points | Presision     | Recall        | Runtime (sec) |
| ----------------------- | -------- |---------------|---------------|-------------- |
| Original (400%)         | `188.000`| `1`           | `1`           |`73.020051405` |
| Original (200%)         | `94.000` | `1`           | `1`           |`49.346912155` |
| Original (All)          | `47.000` | `1`           | `1`           |`42.109486233` |
| Original (50%)          | `23.500` | `1`           | `1`           |`35.566625094` |
| Original (25%)          | `11.750` | `1`           | `1`           |`32.941732741` |
| outliers                | `876`    | `0.6428`      | `1.000`       |`21.45921544`  |



## 5. References



