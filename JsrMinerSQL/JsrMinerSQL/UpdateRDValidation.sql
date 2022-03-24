
SELECT *
  
 --UPDATE o  SET rd_validation = 'FN', rd_id = NULL
  UPDATE o SET location_before = 'test/ng/compileSpec.js:253050-253373'
   FROM dbo.ORACLE AS o

    WHERE commit_id ='38f8c97af74649ce224b6dd45f433cc665acfbfb' AND location_after = 'test/ng/compileSpec.js:243715-244018'
