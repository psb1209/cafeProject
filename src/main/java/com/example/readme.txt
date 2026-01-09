SET @idx :=
(
SELECT s.INDEX_NAME
FROM information_schema.STATISTICS s
WHERE s.TABLE_SCHEMA = DATABASE()
AND s.TABLE_NAME = 'boards'
GROUP BY s.INDEX_NAME
HAVING SUM(s.COLUMN_NAME = 'code') = 1
AND COUNT(*) = 1
AND MIN(s.NON_UNIQUE) = 0
LIMIT 1
);

SET @sql := IF(@idx IS NULL,
'SELECT ''No unique index on boards.code to drop'';',
CONCAT('ALTER TABLE boards DROP INDEX `', @idx, '`;'));

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE boards
ADD CONSTRAINT uk_boards_cafeid_code UNIQUE (cafeid, code);
