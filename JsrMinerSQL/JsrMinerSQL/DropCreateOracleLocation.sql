USE Eval;

DROP TABLE IF EXISTS dbo.LocationTemp1;
DROP TABLE IF EXISTS dbo.LocationTemp2;
DROP TABLE IF EXISTS dbo.LocationTemp3;
DROP TABLE IF EXISTS dbo.OracleLocation;

SELECT *
, LEFT(location_before, CHARINDEX( ':', location_before) - 1) AS file_before
, LEFT(location_after, CHARINDEX( ':', location_after) - 1) AS file_after
, RIGHT(location_before, CHARINDEX(':', REVERSE(location_before) + ':') - 1) AS file_location_before
, RIGHT(location_after, CHARINDEX(':', REVERSE(location_after) + ':') - 1) AS file_location_after

INTO dbo.LocationTemp1
FROM jsrminer.dbo.Oracle;


SELECT id, refactoring_type, file_before, file_after, file_location_before, file_location_after
, CASE WHEN  CHARINDEX( '|', file_location_before) > 0 THEN 1 ELSE NULL END AS has_line_col_before
, CASE WHEN  CHARINDEX( '|', file_location_after) > 0 THEN 1 ELSE NULL END AS has_line_col_after
INTO dbo.LocationTemp2
FROM dbo.LocationTemp1;


-- Split file, line, col of before after info
SELECT id, refactoring_type, file_before, file_after, file_location_before, file_location_after
, CASE 
	WHEN has_line_col_before = 1 
	THEN LEFT(file_location_before, CHARINDEX( '|', file_location_before) - 1)
	ELSE file_location_before
  END AS begin_end_before
, CASE 
	WHEN has_line_col_before = 1
	THEN RIGHT(file_location_before, CHARINDEX('|', REVERSE(file_location_before) + '|') - 1)
  END AS line_col_before
, CASE 
	WHEN has_line_col_after = 1 
	THEN LEFT(file_location_after, CHARINDEX( '|', file_location_after) - 1)
	ELSE file_location_after
  END AS begin_end_after
, CASE 
	WHEN has_line_col_after = 1
	THEN RIGHT(file_location_after, CHARINDEX('|', REVERSE(file_location_after) + '|') - 1)
  END AS line_col_after

INTO dbo.OracleLocation
FROM dbo.LocationTemp2;


SELECT * FROM dbo.OracleLocation;
DROP TABLE IF EXISTS dbo.LocationTemp1;
DROP TABLE IF EXISTS dbo.LocationTemp2;
DROP TABLE IF EXISTS dbo.LocationTemp3;
--DROP TABLE IF EXISTS dbo.OracleLocation;