DROP TABLE IF EXISTS dbo.DataSet;
DROP TABLE IF EXISTS dbo.ProjectValidation;
DROP TABLE IF EXISTS dbo.Result;

SELECT * 
INTO dbo.DataSet
FROM 
(
SELECT *
FROM dbo.Angular

UNION ALL 

SELECT * FROM dbo.ChartJS

UNION ALL 

SELECT * FROM dbo.CreateReactApp
) AS b
ORDER BY Validation DESC

SELECT 
[project] AS Project,
COUNT(*) AS Total,
COUNT(Validation) AS Validated,
SUM(CASE  WHEN [Validation] IS NULL THEN 1 ELSE 0 END) AS UnValidated,
COUNT(CASE [Validation] WHEN 'TP' THEN 1 END) AS TPs,
COUNT(CASE [Validation] WHEN 'FP' THEN 1 END) AS Fps,
COUNT(CASE [Validation] WHEN 'TN' THEN 1 END) AS TNs,
COUNT(CASE [Validation] WHEN 'FN' THEN 1 END) AS FNs
INTO dbo.ProjectValidation 
FROM dbo.DataSet
GROUP BY [project];


SELECT *
, ROUND(CAST(Tps AS FLOAT) / NULLIF((TPS + FPs),0), 2) AS Precision
, ROUND(CAST(Tps AS FLOAT) / NULLIF((TPS + FNs),0), 2) AS Recall

INTO dbo.Result
FROM dbo.ProjectValidation;


SELECT *
FROM dbo.Result;


SELECT COUNT(Project) AS ProjectCount
, SUM(Validated) As Validated
, SUM(UnValidated) As UnValidated
, SUM(Total) AS RefactoringCount
, SUM(TPs) AS Tps
, SUM(Fps) AS Fps
, SUM(TNs) AS TNs
, SUM(Fns) AS Fns
, AVG(Precision) AS Precision
, AVG(Recall) AS Recall
FROM dbo.Result;
