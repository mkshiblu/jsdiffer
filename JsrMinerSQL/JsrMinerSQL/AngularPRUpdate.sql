/****** Script for SelectTopNRows command from SSMS  ******/
--SELECT [rm_validation]
--      ,[rd_validation]
--      ,[rd_id]
--      ,[comment]
--	  , CASE WHEN rd_validation = 'TP'  THEN 'FN' 
--			 WHEN 	rd_validation = 'FP' THEN 'TN'
--	  END AS rm_v2

UPDATE a 
SET rm_validation = CASE WHEN rd_validation = 'TP'  THEN 'FN' 
			 WHEN 	rd_validation = 'FP' THEN 'TN'
	  END 
  FROM [jsrminer].[dbo].[AngularOracle] AS a
  WHERE rm_validation IS NULL AND rd_validation IS NOT NULL