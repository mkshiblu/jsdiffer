/****** Script for SelectTopNRows command from SSMS  ******/

DROP TABLE IF EXISTS dbo.ProjectCommitNotInOracle;
DROP TABLE IF EXISTS dbo.ProjectCommitRandomized;

-- SELECT Commits not in Oracle
SELECT pc.*
INTO dbo.ProjectCommitNotInOracle
FROM [jsrminer].[dbo].[ProjectCommit] AS pc
LEFT JOIN ThesisOracle AS o ON o.commit_id = pc.commit_id  
WHERE o.commit_id IS NULL
ORDER BY pc.project, pc.commit_id;

-- Randomize the project commits per project
SELECT *
	, ROW_NUMBER() OVER(PARTITION BY project ORDER BY NEWID() ASC)  AS rand_row_num
INTO dbo.ProjectCommitRandomized
FROM dbo.ProjectCommitNotInOracle;


-- Select 10 random commits for evaluation
SELECT *
FROM dbo.ProjectCommitRandomized
WHERE rand_row_num <= 10
ORDER BY project, commit_id;

DROP TABLE IF EXISTS dbo.ProjectCommitNotInOracle;
DROP TABLE IF EXISTS dbo.ProjectCommitRandomized;
