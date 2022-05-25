SELECT *
FROM
(
	SELECT *
	, ROW_NUMBER() OVER (PARTITION BY project ORDER BY ref_count DESC, NEWID()) AS row_num
	FROM
	(
		SELECT *
		FROM 
		(
			SELECT project, commit_id, COUNT(refactoring_type) AS ref_count
			FROM dbo.RmPreStaging
			GROUP BY project, commit_id
	
		) AS b
		WHERE ref_count <=10
	) AS x
) AS v
WHERE row_num <=5
