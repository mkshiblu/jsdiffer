/****** Script for SelectTopNRows command from SSMS  ******/

DROP TABLE IF EXISTS dbo.RdRawTemp;
DROP TABLE IF EXISTS dbo.OracleTemp;
DROP TABLE IF EXISTS dbo.RDStaging;

-- Format location of oracle

SELECT [project]
      ,[commit_id]
      ,UPPER(CONCAT([refactoring_type], '_',[node_type])) AS [refactoring_type]
      ,[name_before]
      ,[name_after]
	  , SUBSTRING([location_before], 1, CHARINDEX('|', [location_before]) - 1) AS [location_before]
	  , SUBSTRING([location_after], 1, CHARINDEX('|', [location_after]) - 1) AS [location_after]
INTO dbo.RdRawTemp
FROM dbo.RdRaW;

SELECT id, project, commit_id, refactoring_type
, [name_before]
, [name_after]
, (CASE WHEN location_before LIKE '%|%' THEN SUBSTRING(location_before, 1, CHARINDEX('|', location_before) -1) 
	ELSE 
	location_before
END ) AS location_before
, (CASE WHEN location_after LIKE '%|%' THEN SUBSTRING(location_after, 1, CHARINDEX('|', location_after) -1) 
	ELSE 
	location_after
END ) AS location_after
	  , rm_validation
	  , rd_validation
INTO dbo.OracleTemp
FROM dbo.Oracle;


SELECT *
FROM dbo.OracleTemp AS ot
LEFT JOIN dbo.RdRawTemp AS rt ON (rt.project = ot.project
AND rt.commit_id = ot.commit_id
AND rt.refactoring_type = ot.refactoring_type
AND rt.location_before = ot.location_before
AND rt.[name_before] = ot.[name_before]
AND rt.location_after = ot.location_after
AND rt.[name_after] = ot.[name_after])
WHERE rt.project is NULL AND  rd_validation IS NOT NULL AND rd_validation NOT IN ('TN', 'FN')
ORDER BY ot.project, ot.commit_id, ot.refactoring_type, ot.location_before, ot.location_after;


SELECT *
FROM dbo.OracleTemp AS ot
LEFT JOIN dbo.RdRawTemp AS rt ON (rt.project = ot.project
AND rt.commit_id = ot.commit_id
AND rt.refactoring_type = ot.refactoring_type
AND rt.location_before = ot.location_before
AND rt.[name_before] = ot.[name_before]
AND rt.location_after = ot.location_after
AND rt.[name_after] = ot.[name_after])
WHERE rt.project is NULL AND  rd_validation IS NOT NULL AND rd_validation NOT IN ('TN', 'FN')
ORDER BY ot.project, ot.commit_id, ot.refactoring_type, ot.location_before, ot.location_after;

--SELECT project, commit_id, refactoring_type, name_before, name_after, location_before, location_after
--FROM RdRawTemp WHERE commit_id= '38f8c97af74649ce224b6dd45f433cc665acfbfb' ORDER BY project, commit_id, refactoring_type, name_before, name_after, location_before, location_after;
--SELECT project, commit_id, refactoring_type, name_before, name_after, location_before, location_after 
--FROM OracleTemp WHERE commit_id= '38f8c97af74649ce224b6dd45f433cc665acfbfb' ORDER BY project, commit_id, refactoring_type, name_before, name_after, location_before, location_after;


--SELECT * FROM RdRawTemp WHERE commit_id ='0ce9348bc01811a88e0e19f85e5bf74536ad4dd4' ORDER BY project, commit_id, refactoring_type, name_before, name_after, location_before, location_after;
--DROP TABLE IF EXISTS dbo.RdRawTemp;
--DROP TABLE IF EXISTS dbo.OracleTemp;
