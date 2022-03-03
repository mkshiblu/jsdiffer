DROP TABLE IF EXISTS dbo.Common;
DROP TABLE IF EXISTS dbo.RMStaging;
DROP TABLE IF EXISTS dbo.RDStaging;
DROP TABLE IF EXISTS dbo.OracleStaging

DROP TABLE IF EXISTS dbo.RDUniqueSet;
DROP TABLE IF EXISTS dbo.RMUniqueSet;

SELECT project, commit_id, UPPER(CONCAT(refactoring_type, '_', node_type)) AS refactoring_type
, SUBSTRING([location_before], 2, CHARINDEX('|', [location_before]) -2) AS [location_before]
, [name_before]
, SUBSTRING([location_after], 2, CHARINDEX('|', [location_after]) -2) AS [location_after]
, [name_after]
INTO dbo.RDStaging
FROM dbo.RDChartJS;


SELECT project, commit_id, refactoring_type
, SUBSTRING([location_before], 2, CHARINDEX('|', [location_before]) -2) AS [location_before]
, [name_before]
, SUBSTRING([location_after], 2, CHARINDEX('|', [location_after]) -2) AS [location_after]
, [name_after]
, Refactoring
INTO dbo.RMStaging
FROM dbo.ChartJS;

SELECT 
rd.project, rd.commit_id,   rm. refactoring_type
    ,rm.[location_before]
      ,rd.[name_before]
      ,rm.[location_after]
      ,rd.[name_after]
	  --,rm.[Validation] AS  rm_validation
      --,rd_validation AS  rd_validation
	  --, id AS rd_id
      --,[comment]
	 , Refactoring AS refactoring
INTO dbo.Common
FROM dbo.RDStaging AS rd
INNER JOIN dbo.RMStaging AS rm ON 

rm.commit_Id = rd.commit_id
AND rm.Refactoring_Type = rd.refactoring_type
AND rm.name_Before = rd.name_before
AND rm.name_after = rd.name_after
AND rm.location_after = rd.location_after
AND rm.location_before = rd.location_before;


-- FInd unique in RM
SELECT s.*
INTO dbo.RMUniqueSet
FROM dbo.RMStaging AS s
LEFT JOIN dbo.Common AS c ON 
s.project = c.project
AND s.commit_Id = c.commit_id
AND s.Refactoring_Type = c.refactoring_type
AND s.name_Before = c.name_before
AND s.name_after = c.name_after
AND s.location_after = c.location_after
AND s.location_before = c.location_before
WHERE c.commit_id IS null


-- FInd unique in RD
SELECT s.*
INTO dbo.RDuniqueSet
FROM dbo.RDStaging AS s
LEFT JOIN dbo.Common AS c ON 
s.project = c.project
AND s.commit_Id = c.commit_id
AND s.Refactoring_Type = c.refactoring_type
AND s.name_Before = c.name_before
AND s.name_after = c.name_after
AND s.location_after = c.location_after
AND s.location_before = c.location_before
WHERE c.commit_id IS null;


SELECT *
INTO dbo.OracleStaging
FROM 
(
SELECT project  
      ,[commit_id]
      ,refactoring_type
      ,[location_before]
      ,[name_before]
      ,[location_after]
      ,[name_after]
	  ,NULL AS  rm_validation
      ,NULL AS  rd_validation
	  --, id AS rd_id
      , NULL [comment]
	 , NULL AS refactoring

FROM dbo.RDUniqueSet AS rd
UNION 
SELECT project, commit_Id, refactoring_type, [location_before]
      ,[name_before]
      ,[location_after]
      ,[name_after]
	   ,NULL AS  rm_validation
      ,NULL AS  rd_validation
	  --, rd_id
      ,NULL [comment]
	  , refactoring
	  FROM dbo.RMUniqueSet
UNION
SELECT project, commit_Id, refactoring_type, [location_before]
      ,[name_before]
      ,[location_after]
      ,[name_after]
      , 'TP' AS  rm_validation
      , 'TP' AS rd_validation
	  --, rd_id
      ,NULL AS [comment]  
	  , refactoring
	  FROM dbo.Common

) AS b

ORDER BY 
 project  
      ,[commit_id]
      ,refactoring_type
      ,[location_before]
      ,[name_before]
      ,[location_after]
      ,[name_after]

DROP TABLE IF EXISTS dbo.Common;
DROP TABLE IF EXISTS dbo.RMStaging;
DROP TABLE IF EXISTS dbo.RDStaging;