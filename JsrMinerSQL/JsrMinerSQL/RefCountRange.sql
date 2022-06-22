

SELECT ref_count_range AS [# Reported Refactorings], COUNT(commit_id) AS [# Commits]
FROM 
(
	SELECT commit_id
	,
	CASE
		WHEN ref_count >=11 AND ref_count <= 20 THEN '11-20'
		WHEN ref_count = 5 THEN '5'
		WHEN ref_count = 4 THEN '4'
		WHEN ref_count = 3 THEN '3'
		WHEN ref_count = 2 THEN '2'
		WHEN ref_count = 1 THEN '1'
		WHEN ref_count >=6 AND ref_count <= 10 THEN '06-10'
		WHEN ref_count >=21 THEN 'more than 20'
	END

	AS ref_count_range
	FROM
	(
		SELECT [commit_id], COUNT(*) AS ref_count
		FROM [Eval].[dbo].[RmPreStaging]
		GROUP BY [commit_id]
	) AS b
) AS d
GROUP BY ref_count_range
ORDER BY LEN(ref_count_range), ref_count_range
