DROP TABLE IF EXISTS dbo.OracleTemp;
DROP TABLE IF EXISTS dbo.ValidationByProject;
DROP TABLE IF EXISTS dbo.PRByProjects;

SELECT *
FROM dbo.Oracle AS o
LEFT JOIN dbo.RefactoringType AS rt ON (rt.name = o.refactoring_type)
WHERE rt.id IS NULL;



SELECT *
FROM dbo.Oracle AS o
INNER JOIN dbo.RefactoringType AS rt ON (rt.name = o.refactoring_type AND rt.supported_by_rd = 'Y' AND rt.supported_by_rm = 'Y');


-- PR BY Projects on Supported Refactorings
SELECT project,
	COUNT(*) AS Total,
	--COUNT(ISNULL(rm_validation, [rd_validation])) AS Validated,
	COUNT(rm_validation) AS rm_validated_count,
	COUNT(rd_validation) AS rd_validated_count,
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


-- Calculate Weighted Overall Average
SELECT COUNT(Project) AS ProjectCount
	, SUM(Total) AS RefactoringCount
	, SUM(UnValidated) As UnValidated
	, SUM(rm_validated_count) AS rm_validated_count
	, SUM(rd_validated_count) AS rd_validated_count

	, SUM(RmTPs) AS RmTPs
	, SUM(RmFps) AS RmFps
	, SUM(RmTNs) AS RmTNs
	, SUM(RdFNs) AS RdFNs

	, SUM(RdTPs) AS RdTPs
	, SUM(RdFps) AS RdFps
	, SUM(RdTNs) AS RdTNs
	, SUM(RmFNs) AS RmFNs
	
	, ROUND(SUM(RmPrecision * rm_validated_count) / SUM(rm_validated_count), 2) AS RmPrecision
	, ROUND(SUM(RmRecall * rm_validated_count) / SUM(rm_validated_count), 2) AS RmRecall
	
	, ROUND(SUM(RdPrecision * rd_validated_count) / SUM(rd_validated_count), 2) AS RdPrecision
	, ROUND(SUM(RdRecall * rd_validated_count) / SUM(rd_validated_count), 2) AS RdRecall
FROM dbo.PRByProjects;


SELECT ((0.93 * 92) + (38 * 0.83) + (62 * 0.9)) / (92 + 38 + 62) AS RmPrecision, 
 ((0.31 * 92) + (38 * 0.19) + (62 * 0.26)) / (92 + 38 + 62) AS RmRecall
, ((0.91 * 85) + (36 * 0.95) + (62 * 0.57)) / (85 + 36 + 62) AS RdPrecision
, ((0.81 * 85) + (36 * 0.88) + (62 * 0.94)) / (85 + 36 + 62) AS RdRecall;

DROP TABLE IF EXISTS dbo.ValidationByProject;
DROP TABLE IF EXISTS dbo.PRByProjects;

