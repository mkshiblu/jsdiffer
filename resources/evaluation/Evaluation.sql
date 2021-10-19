/****** Script for SelectTopNRows command from SSMS  ******/

/****** Script for SelectTopNRows command from SSMS  ******/
SELECT id =  IDENTITY(INT, 1,1),
REPLACE(repository,'.git', '') AS repository
      ,commit_id AS commit_id
      ,refactoring_type AS refactoring_type
      ,node_type AS node_type
      ,location_before AS location_before
      ,ISNULL(name_before, '') AS name_before
      ,location_after AS location_after
      ,ISNULL(name_after, '') AS name_after
	  INTO dbo.RdData
  FROM [jsrminer].[dbo].data
  ORDER BY repository, commit_id, refactoring_type, node_type, location_before

SELECT COUNT(*)
  FROM [jsrminer].[dbo].[RDData]


SELECT *
FROM [jsrminer].[dbo].[RDData]
WHERE name_before  IS NULL