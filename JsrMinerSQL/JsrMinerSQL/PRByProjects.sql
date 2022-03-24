DROP TABLE IF EXISTS dbo.ValidationByProject;
DROP TABLE IF EXISTS dbo.PRByProjects;

-- PR BY Projects
SELECT project,
	COUNT(*) AS Total,
	COUNT(ISNULL(rm_validation, [rd_validation])) AS Validated,
	SUM(CASE  WHEN ISNULL(rm_validation, [rd_validation]) IS NULL THEN 1 ELSE 0 END) AS UnValidated,
	COUNT(CASE rm_validation WHEN 'TP' THEN 1 END) AS RmTPs,
	COUNT(CASE rm_validation WHEN 'FP' THEN 1 END) AS RmFps,
	COUNT(CASE rm_validation WHEN 'TN' THEN 1 END) AS RmTNs,
	COUNT(CASE rm_validation WHEN 'FN' THEN 1 END) AS RmFNs,
	COUNT(CASE [rd_validation] WHEN 'TP' THEN 1 END) AS RdTPs,
	COUNT(CASE [rd_validation] WHEN 'FP' THEN 1 END) AS RdFps,
	COUNT(CASE [rd_validation] WHEN 'TN' THEN 1 END) AS RdTNs,
	COUNT(CASE [rd_validation] WHEN 'FN' THEN 1 END) AS RdFNs
INTO dbo.ValidationByProject 
FROM dbo.Oracle
GROUP BY [project];


SELECT *
, ROUND(CAST(RmTPs AS FLOAT) / NULLIF((RmTPs + RmFps),0), 2) AS RmPrecision
, ROUND(CAST(RmTPs AS FLOAT) / NULLIF((RmTPs + RmFNs),0), 2) AS RmRecall

, ROUND(CAST(RdTPs AS FLOAT) / NULLIF((RdTPs + RdFps),0), 2) AS RdPrecision
, ROUND(CAST(RdTPs AS FLOAT) / NULLIF((RdTPs + RdFNs),0), 2) AS RdRecall
INTO dbo.PRByProjects
FROM dbo.ValidationByProject;


SELECT *
FROM dbo.PRByProjects;


SELECT COUNT(Project) AS ProjectCount
, SUM(Validated) As Validated
, SUM(UnValidated) As UnValidated
, SUM(Total) AS RefactoringCount
, SUM(RmTPs) AS RmTPs
, SUM(RmFps) AS RmFps
, SUM(RmTNs) AS RmTNs
, SUM(RdFNs) AS RdFNs

, SUM(RdTPs) AS RdTPs
, SUM(RdFps) AS RdFps
, SUM(RdTNs) AS RdTNs
, SUM(RmFNs) AS RmFNs
, ROUND(AVG(RmPrecision), 2) AS RmPrecision
, ROUND(AVG(RmRecall), 2) AS RmRecall
, ROUND(AVG(RdPrecision), 2) AS RdPrecision
, ROUND(AVG(RdRecall), 2) AS RdRecall
FROM dbo.PRByProjects;

DROP TABLE IF EXISTS dbo.ValidationByProject;
DROP TABLE IF EXISTS dbo.PRByProjects;