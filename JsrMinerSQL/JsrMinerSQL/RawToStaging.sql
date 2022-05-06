DROP TABLE IF EXISTS dbo.RawTemp;
DROP TABLE IF EXISTS dbo.Common;
DROP TABLE IF EXISTS dbo.AddedInRaw;
DROP TABLE IF EXISTS dbo.RemvoedFromOracle;

SELECT project, commit_id, refactoring_type
, SUBSTRING([location_before], 2, CHARINDEX('|', [location_before]) -2) AS [location_before]
, [name_before]
, SUBSTRING([location_after], 2, CHARINDEX('|', [location_after]) -2) AS [location_after]
, [name_after]
INTO dbo.RawTemp
FROM dbo.ChartJS_Raw;

UPDATE RawTemp
SET Refactoring_Type  = REPLACE(REPLACE(Refactoring_Type, 'OPERATION', 'FUNCTION'), 'METHOD', 'FUNCTION');

-- Common
SELECT o.*
, new_validation_raw
FROM dbo.ChartJSOracle AS o
INNER  JOIN dbo.RawTemp AS RT ON 
RT.project = o.project AND RT.commit_Id = o.commit_Id 
 AND o.[Refactoring_Type] = RT. [Refactoring_Type]

  AND RT.[Name_Before] = o.[Name_Before] 
  
  AND RT.Name_After = o.Name_After 
  
  AND RT.[Location_Before] = o.[Location_Before]

    AND RT.[Location_After] = o.[Location_After];



SELECT * 
FROM dbo.ChartJSOracle AS o
LEFT JOIN dbo.RawTemp AS RT ON 
RT.project = o.project AND RT.commit_Id = o.commit_Id 
 AND o.[Refactoring_Type] = RT. [Refactoring_Type]

  AND RT.[Name_Before] = o.[Name_Before] 
  
  AND RT.Name_After = o.Name_After 
  
  AND RT.[Location_Before] = o.[Location_Before]

    AND RT.[Location_After] = o.[Location_After]




--SELECT * FROM ChartJS_Raw WHERE commit_id = '182270ef9b1bc9fab1cefc89ce1e94a41f4d754f'
--SELECT * FROM ChartJSOracle WHERE commit_id = '182270ef9b1bc9fab1cefc89ce1e94a41f4d754f'

DROP TABLE IF EXISTS dbo.RawTemp;



