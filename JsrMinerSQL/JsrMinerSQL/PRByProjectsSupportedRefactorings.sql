DROP TABLE IF EXISTS dbo.ValidationByProject;
DROP TABLE IF EXISTS dbo.PRByProjects;

-- PR BY Projects
SELECT project,
	COUNT(*) AS Total,
	COUNT(ISNULL(rm_validation, [rd_validation])) AS Validated,
	SUM(CASE  WHEN ISNULL(rm_validation, [rd_validation]) IS NULL THEN 1 ELSE 0 END) AS UnValidated,
	SUM(CASE WHEN supported_by_rm = 'Y' AND rm_validation = 'TP' THEN 1 END) AS RmTps,
	SUM(CASE WHEN supported_by_rm = 'Y' AND rm_validation = 'FP' THEN 1 END) AS RmFps,
	SUM(CASE WHEN supported_by_rm = 'Y' AND rm_validation = 'TN' THEN 1 END) AS RmTNs,
	SUM(CASE WHEN supported_by_rm = 'Y' AND rm_validation = 'FN' THEN 1 END) AS RmFNs,

	SUM(CASE WHEN supported_by_rd = 'Y' AND rd_validation = 'TP' THEN 1 END) AS RdTPs,
	SUM(CASE WHEN supported_by_rd = 'Y' AND rd_validation = 'FP' THEN 1 END) AS RdFps,
	SUM(CASE WHEN supported_by_rd = 'Y' AND rd_validation = 'TN' THEN 1 END) AS RdTNs,
	SUM(CASE WHEN supported_by_rd = 'Y' AND rd_validation = 'FN' THEN 1 END) AS RdFNs
INTO dbo.ValidationByProject 
FROM dbo.Oracle AS o
LEFT JOIN dbo.RefactoringType AS r ON (r.[name] = o.[refactoring_type])
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