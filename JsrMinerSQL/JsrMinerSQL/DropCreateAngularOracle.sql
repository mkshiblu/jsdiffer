DROP TABLE IF EXISTS dbo.Common;
DROP TABLE IF EXISTS dbo.RDUniqueSet;
DROP TABLE IF EXISTS dbo.RMUniqueSet;
DROP TABLE IF EXISTS dbo.AngularOracle;

SELECT *
INTO dbo.Common
FROM Angular AS a
WHERE rd_id IS NOT NULL;


SELECT rd.* 
INTO dbo.RDUniqueSet
FROM RDData AS rd
LEFT JOIN dbo.Common AS c ON (c.rd_id = rd.id)
WHERE repository LIKE '%angular%' AND c.rd_id IS NULL;


SELECT * 
INTO dbo.RMUniqueSet
FROM dbo.Angular 
WHERE  rd_id IS NULL;




SELECT *
INTO dbo.AngularOracle
FROM 
(
SELECT repository as project  
      ,[commit_id]
      , UPPER(CONCAT(refactoring_type, '_', node_type)) AS refactoring_type
      ,[location_before]
      ,[name_before]
      ,[location_after]
      ,[name_after]
	  ,NULL AS  rm_validation
      ,[validation] AS  rd_validation
	  , id AS rd_id
      ,[comment]
	 , NULL AS refactoring

FROM dbo.RDUniqueSet AS rd
UNION 
SELECT project, commit_Id, refactoring_type, [location_before]
      ,[name_before]
      ,[location_after]
      ,[name_after]
      
	  ,[validation] AS  rm_validation
      ,rd_validation 
	  , rd_id
      ,[comment]
	  , refactoring
	  FROM dbo.RMUniqueSet
UNION
SELECT project, commit_Id, refactoring_type, [location_before]
      ,[name_before]
      ,[location_after]
      ,[name_after]
      ,[validation] AS  rm_validation
      ,rd_validation 
	  , rd_id
      ,[comment]  
	  , refactoring
	  FROM dbo.Common

) AS b;


