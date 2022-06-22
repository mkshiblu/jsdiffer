USE Eval;


DROP TABLE IF EXISTS dbo.LocationTemp1;
DROP TABLE IF EXISTS dbo.LocationTemp2;
DROP TABLE IF EXISTS dbo.LocationTemp3;
DROP TABLE IF EXISTS dbo.RmPreStagingLocationTemp;

SELECT *, rm_id AS id
, LEFT(location_before, CHARINDEX( ':', location_before) - 1) AS file_before
, LEFT(location_after, CHARINDEX( ':', location_after) - 1) AS file_after
, RIGHT(location_before, CHARINDEX(':', REVERSE(location_before) + ':') - 1) AS file_location_before
, RIGHT(location_after, CHARINDEX(':', REVERSE(location_after) + ':') - 1) AS file_location_after

INTO dbo.OracleLocationTemp1
FROM dbo.RmPreStaging

SELECT id, file_before, file_after, file_location_before, file_location_after
, CASE WHEN  CHARINDEX( '|', file_location_before) > 0 THEN 1 ELSE NULL END AS has_line_col_before
, CASE WHEN  CHARINDEX( '|', file_location_after) > 0 THEN 1 ELSE NULL END AS has_line_col_after
INTO dbo.OracleLocationTemp2
FROM dbo.OracleLocationTemp1;


-- Split file, line, col of before after info
SELECT id AS rm_id, file_before, file_after, file_location_before, file_location_after
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

INTO dbo.RmPreStagingLocationTemp
FROM dbo.OracleLocationTemp2;


SELECT * FROM dbo.RmPreStagingLocationTemp;
DROP TABLE IF EXISTS dbo.OracleLocationTemp1;
DROP TABLE IF EXISTS dbo.OracleLocationTemp2;
DROP TABLE IF EXISTS dbo.OracleLocationTemp3;
--DROP TABLE IF EXISTS dbo.RmPreStagingLocationTemp;