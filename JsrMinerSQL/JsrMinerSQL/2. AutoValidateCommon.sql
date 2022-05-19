/****** Script for SelectTopNRows command from SSMS  ******/

DROP TABLE IF EXISTS dbo.OracleTemp;

SELECT *
INTO dbo.OracleTemp
FROM [jsrminer].dbo.RDData AS rd

WHERE commit_id IN
  (
	SELECT DISTINCT commit_id
	FROM [Eval].[dbo].[RmRaw] 
  )
  ORDER by repository, commit_id, refactoring_type
;

SELECT * FROM dbo.OracleTemp;
SELECT * FROM [Eval].[dbo].[RmRaw] 


