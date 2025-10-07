-- 新增遺失數量欄位到庫存表
ALTER TABLE inv_stock ADD COLUMN lost_qty INT(11) DEFAULT 0 COMMENT '遺失數量' AFTER damaged_qty;
