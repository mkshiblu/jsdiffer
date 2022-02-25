DROP TABLE IF EXISTS dbo.AngularOracle;
DROP TABLE IF EXISTS dbo.Common;

--SELECT *
UPDATE rma
SET rd_id = id
FROM 
(
SELECT rd.*
FROM dbo.RDData AS rd
WHERE repository LIKE '%angular%'
) AS rda

INNER JOIN Angular AS rma
	ON (rma.project = rda.repository 
	AND rma.commit_id = rda.commit_id 
	AND rma.refactoring_type = CONCAT(rda.refactoring_type, '_', rda.node_type)
	AND rma.name_before = rda.name_before 
    AND rma.name_after = rda.name_after 
	AND SUBSTRING(rma.location_after, 2, CHARINDEX('|', rma.location_after) -2) = rda.Location_after
	AND SUBSTRING(rma.location_before, 2, CHARINDEX('|', rma.location_before) -2) = rda.location_before
)  
