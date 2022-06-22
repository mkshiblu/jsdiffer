USE Eval;

DROP TABLE IF EXISTS dbo.RmCommonTypeProjectCommit;
DROP TABLE IF EXISTS dbo.RdCommonTypeProjectCommit;
DROP TABLE IF EXISTS dbo.ProjectCommitWithSupportedType;
DROP TABLE IF EXISTS dbo.RdCommonTemp;
DROP TABLE IF EXISTS dbo.RmCommonTemp;
DROP TABLE IF EXISTS dbo.RmCommitRefCount;
DROP TABLE IF EXISTS dbo.RdCommitRefCount;
DROP TABLE IF EXISTS dbo.ProjectCommitLessThan10Ref;

-- Take the commits with atleast one common refactorings from RMRaw
SELECT r.project, r.commit_id, COUNT(rm_id) AS rm_count
INTO dbo.RmCommonTypeProjectCommit
FROM dbo.RmPreStaging AS r
LEFT JOIN [jsrminer].dbo.[RefactoringType] AS rt ON (rt.[name] = r.refactoring_type AND rt.supported_by_rm = 'Y' AND rt.supported_by_rd = 'Y')
WHERE rt.id IS NOT NULL
GROUP BY r.project, r.commit_id
ORDER BY rm_count DESC;


-- Take the commits with atleast one common refactorings from RDRaw
SELECT r.project, r.commit_id, COUNT(rd_id) AS rd_count
INTO dbo.RdCommonTypeProjectCommit
FROM dbo.RdPreStaging AS r
LEFT JOIN [jsrminer].dbo.[RefactoringType] AS rt ON (rt.[name] = r.[refactoring_type] AND rt.supported_by_rm = 'Y' AND rt.supported_by_rd = 'Y')
WHERE rt.id IS NOT NULL
GROUP BY r.project, r.commit_id
ORDER BY rd_count DESC;


-- Union project, commit_id
SELECT project, [commit_id]
INTO ProjectCommitWithSupportedType
FROM
(
	SELECT project, commit_id FROM dbo.RmCommonTypeProjectCommit
	UNION
	SELECT project, commit_id FROM dbo.RdCommonTypeProjectCommit
) AS r
GROUP BY project, commit_id
ORDER BY project, commit_id;


-- FILTER Unique types in RD
SELECT rd.*
INTO dbo.RdCommonTemp
FROM
(
	SELECT r.*
	FROM dbo.RdPreStaging AS r
	LEFT JOIN [jsrminer].dbo.[RefactoringType] AS rt ON (rt.[name] = r.refactoring_type AND rt.supported_by_rm = 'Y' AND rt.supported_by_rd = 'Y')
	WHERE rt.id IS NOT NULL
) AS rd
INNER JOIN dbo.ProjectCommitWithSupportedType AS pcs ON (pcs.project = rd.project AND pcs.commit_id = rd.commit_id);


-- FILTER Unique types in RM
SELECT rm.*
INTO dbo.RmCommonTemp
FROM
(
	SELECT r.*
	FROM dbo.RmPreStaging AS r
	LEFT JOIN [jsrminer].dbo.[RefactoringType] AS rt ON (rt.[name] = r.refactoring_type AND rt.supported_by_rm = 'Y' AND rt.supported_by_rd = 'Y')
	WHERE rt.id IS NOT NULL
) AS rm
INNER JOIN dbo.ProjectCommitWithSupportedType AS pcs ON (pcs.project = rm.project AND pcs.commit_id = rm.commit_id);


-- Find ref_count per commit
SELECT project, commit_id, COUNT(rd_id) AS ref_count
INTO dbo.RdCommitRefCount
FROM dbo.RdCommonTemp
GROUP BY project, commit_id
ORDER BY ref_count DESC;


SELECT project, commit_id, COUNT(rm_id) AS ref_count
INTO dbo.RmCommitRefCount
FROM dbo.RmCommonTemp
GROUP BY project, commit_id
ORDER BY ref_count DESC;


-- Take ref count <=10
SELECT project, commit_id
INTO dbo.ProjectCommitLessThan10Ref
FROM
(
	SELECT project, commit_id
	FROM dbo.RdCommitRefCount
	WHERE ref_count <=10
	UNION 
	SELECT project, commit_id
	FROM dbo.RmCommitRefCount
	WHERE ref_count <=10
) AS b

SELECT *
--INTO dbo.Random5CommitsEachProject
FROM
(
	SELECT *
	, ROW_NUMBER() OVER (PARTITION BY project ORDER BY NEWID()) AS row_num
	FROM dbo.ProjectCommitLessThan10Ref
) AS v
WHERE row_num <=5;


DROP TABLE IF EXISTS dbo.RmCommonTypeProjectCommit;
DROP TABLE IF EXISTS dbo.RdCommonTypeProjectCommit;
DROP TABLE IF EXISTS dbo.ProjectCommitWithSupportedType;
DROP TABLE IF EXISTS dbo.RdCommonTemp;
DROP TABLE IF EXISTS dbo.RmCommonTemp;
DROP TABLE IF EXISTS dbo.RmCommitRefCount;
DROP TABLE IF EXISTS dbo.RdCommitRefCount;
DROP TABLE IF EXISTS dbo.ProjectCommitLessThan10Ref;