INSERT INTO dbo.Oracle 

([project]
      ,[commit_id]
      ,[refactoring_type]
      ,[name_before]
      ,[name_after]
      ,[location_before]
      ,[location_after])

SELECT [project]
      ,[commit_id]
      ,[refactoring_type]
      ,[name_before]
      ,[name_after]
      ,[location_before]
      ,[location_after]
FROM dbo.RDStaging
