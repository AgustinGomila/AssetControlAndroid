CREATE TABLE IF NOT EXISTS `asset`
(
    `_id`
    INTEGER
    PRIMARY
    KEY
    AUTOINCREMENT
    NOT
    NULL,
    `code`
    TEXT
    NOT
    NULL,
    `description`
    TEXT
    NOT
    NULL,
    `warehouse_id`
    INTEGER
    NOT
    NULL,
    `warehouse_area_id`
    INTEGER
    NOT
    NULL,
    `active`
    INTEGER
    NOT
    NULL
    DEFAULT
    1,
    `ownership_status`
    INTEGER
    NOT
    NULL
    DEFAULT
    1,
    `status`
    INTEGER
    NOT
    NULL
    DEFAULT
    1,
    `missing_date`
    TEXT,
    `item_category_id`
    INTEGER
    NOT
    NULL
    DEFAULT
    0,
    `transferred`
    INTEGER,
    `original_warehouse_id`
    INTEGER
    NOT
    NULL,
    `original_warehouse_area_id`
    INTEGER
    NOT
    NULL,
    `label_number`
    INTEGER,
    `manufacturer`
    TEXT,
    `model`
    TEXT,
    `serial_number`
    TEXT,
    `condition`
    INTEGER,
    `cost_centre_id`
    INTEGER,
    `parent_id`
    INTEGER,
    `ean`
    TEXT,
    `last_asset_review_date`
    TEXT
);

CREATE INDEX IF NOT EXISTS `IDX_asset_code` ON `asset` (`code`);
CREATE INDEX IF NOT EXISTS `IDX_asset_description` ON `asset` (`description`);
CREATE INDEX IF NOT EXISTS `IDX_asset_item_category_id` ON `asset` (`item_category_id`);
CREATE INDEX IF NOT EXISTS `IDX_asset_warehouse_id` ON `asset` (`warehouse_id`);
CREATE INDEX IF NOT EXISTS `IDX_asset_warehouse_area_id` ON `asset` (`warehouse_area_id`);
CREATE INDEX IF NOT EXISTS `IDX_asset_serial_number` ON `asset` (`serial_number`);
CREATE INDEX IF NOT EXISTS `IDX_asset_ean` ON `asset` (`ean`);

CREATE TABLE IF NOT EXISTS `asset_maintenance`
(
    `_id`
    INTEGER
    NOT
    NULL,
    `asset_id`
    INTEGER
    NOT
    NULL,
    `observations`
    TEXT,
    `transferred`
    INTEGER,
    `maintenance_status_id`
    INTEGER
    NOT
    NULL,
    `asset_maintenance_id`
    INTEGER
    NOT
    NULL,
    `maintenance_type_id`
    INTEGER
    NOT
    NULL,
    PRIMARY
    KEY
(
    `_id`
)
    );

CREATE INDEX IF NOT EXISTS `IDX_asset_maintenance_asset_id` ON `asset_maintenance` (`asset_id`);
CREATE INDEX IF NOT EXISTS `IDX_asset_maintenance_maintenance_status_id` ON `asset_maintenance` (`maintenance_status_id`);
CREATE INDEX IF NOT EXISTS `IDX_asset_maintenance_asset_maintenance_id` ON `asset_maintenance` (`asset_maintenance_id`);
CREATE INDEX IF NOT EXISTS `IDX_asset_maintenance_maintenance_type_id` ON `asset_maintenance` (`maintenance_type_id`);
CREATE INDEX IF NOT EXISTS `IDX_asset_maintenance__id` ON `asset_maintenance` (`_id`);

CREATE TABLE IF NOT EXISTS `asset_review`
(
    `_id`
    INTEGER
    PRIMARY
    KEY
    AUTOINCREMENT
    NOT
    NULL,
    `asset_review_date`
    INTEGER
    NOT
    NULL,
    `obs`
    TEXT,
    `user_id`
    INTEGER
    NOT
    NULL,
    `warehouse_area_id`
    INTEGER
    NOT
    NULL,
    `warehouse_id`
    INTEGER
    NOT
    NULL,
    `modification_date`
    INTEGER
    NOT
    NULL,
    `status_id`
    INTEGER
    NOT
    NULL
);

CREATE INDEX IF NOT EXISTS `IDX_asset_review_user_id` ON `asset_review` (`user_id`);
CREATE INDEX IF NOT EXISTS `IDX_asset_review_warehouse_area_id` ON `asset_review` (`warehouse_area_id`);
CREATE INDEX IF NOT EXISTS `IDX_asset_review_warehouse_id` ON `asset_review` (`warehouse_id`);

CREATE TABLE IF NOT EXISTS `asset_review_content`
(
    `_id`
    INTEGER
    PRIMARY
    KEY
    AUTOINCREMENT
    NOT
    NULL,
    `asset_review_id`
    INTEGER
    NOT
    NULL,
    `asset_id`
    INTEGER
    NOT
    NULL,
    `code`
    TEXT
    NOT
    NULL,
    `description`
    TEXT
    NOT
    NULL,
    `qty`
    REAL,
    `content_status_id`
    INTEGER
    NOT
    NULL,
    `origin_warehouse_area_id`
    INTEGER
    NOT
    NULL
);

CREATE INDEX IF NOT EXISTS `IDX_asset_review_content_asset_review_id` ON `asset_review_content` (`asset_review_id`);
CREATE INDEX IF NOT EXISTS `IDX_asset_review_content_asset_id` ON `asset_review_content` (`asset_id`);
CREATE INDEX IF NOT EXISTS `IDX_asset_review_content_code` ON `asset_review_content` (`code`);
CREATE INDEX IF NOT EXISTS `IDX_asset_review_content_description` ON `asset_review_content` (`description`);
CREATE INDEX IF NOT EXISTS `IDX_asset_review_content_origin_warehouse_area_id` ON `asset_review_content` (`origin_warehouse_area_id`);

CREATE TABLE IF NOT EXISTS `attribute`
(
    `_id`
    INTEGER
    NOT
    NULL,
    `description`
    TEXT
    NOT
    NULL,
    `active`
    INTEGER
    NOT
    NULL,
    `attribute_type_id`
    INTEGER
    NOT
    NULL,
    `attribute_category_id`
    INTEGER
    NOT
    NULL,
    PRIMARY
    KEY
(
    `_id`
)
    );

CREATE INDEX IF NOT EXISTS `IDX_attribute_attribute_type_id` ON `attribute` (`attribute_type_id`);
CREATE INDEX IF NOT EXISTS `IDX_attribute_attribute_category_id` ON `attribute` (`attribute_category_id`);
CREATE INDEX IF NOT EXISTS `IDX_attribute_description` ON `attribute` (`description`);

CREATE TABLE IF NOT EXISTS `attribute_category`
(
    `_id`
    INTEGER
    NOT
    NULL,
    `description`
    TEXT
    NOT
    NULL,
    `active`
    INTEGER
    NOT
    NULL,
    `parent_id`
    INTEGER
    NOT
    NULL,
    PRIMARY
    KEY
(
    `_id`
)
    );

CREATE INDEX IF NOT EXISTS `IDX_attribute_category_parent_id` ON `attribute_category` (`parent_id`);
CREATE INDEX IF NOT EXISTS `IDX_attribute_category_description` ON `attribute_category` (`description`);

CREATE TABLE IF NOT EXISTS `attribute_composition`
(
    `_id`
    INTEGER
    NOT
    NULL,
    `attribute_id`
    INTEGER
    NOT
    NULL,
    `attribute_composition_type_id`
    INTEGER
    NOT
    NULL,
    `description`
    TEXT,
    `composition`
    TEXT,
    `used`
    INTEGER
    NOT
    NULL,
    `name`
    TEXT
    NOT
    NULL,
    `read_only`
    INTEGER
    NOT
    NULL,
    `default_value`
    TEXT
    NOT
    NULL,
    PRIMARY
    KEY
(
    `_id`
)
    );

CREATE INDEX IF NOT EXISTS `IDX_attribute_composition_attribute_id` ON `attribute_composition` (`attribute_id`);
CREATE INDEX IF NOT EXISTS `IDX_attribute_composition_attribute_composition_type_id` ON `attribute_composition` (`attribute_composition_type_id`);
CREATE INDEX IF NOT EXISTS `IDX_attribute_composition_description` ON `attribute_composition` (`description`);

CREATE TABLE IF NOT EXISTS `barcode_label_custom`
(
    `_id`
    INTEGER
    NOT
    NULL,
    `description`
    TEXT
    NOT
    NULL,
    `active`
    INTEGER
    NOT
    NULL,
    `barcode_label_target_id`
    INTEGER
    NOT
    NULL,
    `template`
    TEXT
    NOT
    NULL,
    PRIMARY
    KEY
(
    `_id`
)
    );

CREATE INDEX IF NOT EXISTS `IDX_barcode_label_custom_barcode_label_target_id` ON `barcode_label_custom` (`barcode_label_target_id`);
CREATE INDEX IF NOT EXISTS `IDX_barcode_label_custom_description` ON `barcode_label_custom` (`description`);

CREATE TABLE IF NOT EXISTS `barcode_label_target`
(
    `_id`
    INTEGER
    NOT
    NULL,
    `description`
    TEXT
    NOT
    NULL,
    PRIMARY
    KEY
(
    `_id`
)
    );
CREATE INDEX IF NOT EXISTS `IDX_barcode_label_target__id` ON `barcode_label_target` (`_id`);
CREATE INDEX IF NOT EXISTS `IDX_barcode_label_target_description` ON `barcode_label_target` (`description`);

CREATE TABLE IF NOT EXISTS `data_collection`
(
    `_id`
    INTEGER
    PRIMARY
    KEY
    AUTOINCREMENT
    NOT
    NULL,
    `data_collection_id`
    INTEGER
    NOT
    NULL,
    `asset_id`
    INTEGER,
    `warehouse_id`
    INTEGER,
    `warehouse_area_id`
    INTEGER,
    `user_id`
    INTEGER
    NOT
    NULL,
    `date_start`
    INTEGER,
    `date_end`
    INTEGER,
    `completed`
    INTEGER
    NOT
    NULL,
    `transferred_date`
    INTEGER,
    `route_process_id`
    INTEGER
    NOT
    NULL
);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_data_collection_id` ON `data_collection` (`data_collection_id`);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_asset_id` ON `data_collection` (`asset_id`);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_warehouse_id` ON `data_collection` (`warehouse_id`);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_warehouse_area_id` ON `data_collection` (`warehouse_area_id`);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_user_id` ON `data_collection` (`user_id`);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_route_process_id` ON `data_collection` (`route_process_id`);

CREATE TABLE IF NOT EXISTS `data_collection_content`
(
    `_id`
    INTEGER
    PRIMARY
    KEY
    AUTOINCREMENT
    NOT
    NULL,
    `data_collection_content_id`
    INTEGER
    NOT
    NULL,
    `data_collection_id`
    INTEGER,
    `level`
    INTEGER,
    `position`
    INTEGER,
    `attribute_id`
    INTEGER,
    `attribute_composition_id`
    INTEGER,
    `result`
    INTEGER,
    `value_str`
    TEXT
    NOT
    NULL,
    `data_collection_date`
    INTEGER
    NOT
    NULL,
    `data_collection_rule_content_id`
    INTEGER
    NOT
    NULL
);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_content_data_collection_id` ON `data_collection_content` (`data_collection_id`);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_content_level` ON `data_collection_content` (`level`);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_content_position` ON `data_collection_content` (`position`);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_content_attribute_id` ON `data_collection_content` (`attribute_id`);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_content_attribute_composition_id` ON `data_collection_content` (`attribute_composition_id`);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_content_data_collection_content_id` ON `data_collection_content` (`data_collection_content_id`);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_content_data_collection_rule_content_id` ON `data_collection_content` (`data_collection_rule_content_id`);

CREATE TABLE IF NOT EXISTS `data_collection_rule`
(
    `_id`
    INTEGER
    NOT
    NULL,
    `description`
    TEXT
    NOT
    NULL,
    `active`
    INTEGER
    NOT
    NULL,
    PRIMARY
    KEY
(
    `_id`
)
    );
CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_description` ON `data_collection_rule` (`description`);

CREATE TABLE IF NOT EXISTS `data_collection_rule_content`
(
    `_id`
    INTEGER
    NOT
    NULL,
    `data_collection_rule_id`
    INTEGER
    NOT
    NULL,
    `level`
    INTEGER
    NOT
    NULL,
    `position`
    INTEGER
    NOT
    NULL,
    `attribute_id`
    INTEGER
    NOT
    NULL,
    `attribute_composition_id`
    INTEGER
    NOT
    NULL,
    `expression`
    TEXT,
    `true_result`
    INTEGER
    NOT
    NULL,
    `false_result`
    INTEGER
    NOT
    NULL,
    `description`
    TEXT
    NOT
    NULL,
    `active`
    INTEGER
    NOT
    NULL,
    `mandatory`
    INTEGER
    NOT
    NULL,
    PRIMARY
    KEY
(
    `_id`
)
    );
CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_content_data_collection_rule_id` ON `data_collection_rule_content` (`data_collection_rule_id`);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_content_level` ON `data_collection_rule_content` (`level`);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_content_position` ON `data_collection_rule_content` (`position`);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_content_attribute_id` ON `data_collection_rule_content` (`attribute_id`);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_content_attribute_composition_id` ON `data_collection_rule_content` (`attribute_composition_id`);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_content_description` ON `data_collection_rule_content` (`description`);

CREATE TABLE IF NOT EXISTS `data_collection_rule_target`
(
    `_id`
    INTEGER
    PRIMARY
    KEY
    AUTOINCREMENT
    NOT
    NULL,
    `data_collection_rule_id`
    INTEGER
    NOT
    NULL,
    `asset_id`
    INTEGER,
    `warehouse_id`
    INTEGER,
    `warehouse_area_id`
    INTEGER,
    `item_category_id`
    INTEGER
);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_target_data_collection_rule_id` ON `data_collection_rule_target` (`data_collection_rule_id`);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_target_asset_id` ON `data_collection_rule_target` (`asset_id`);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_target_warehouse_id` ON `data_collection_rule_target` (`warehouse_id`);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_target_warehouse_area_id` ON `data_collection_rule_target` (`warehouse_area_id`);
CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_target_item_category_id` ON `data_collection_rule_target` (`item_category_id`);

CREATE TABLE IF NOT EXISTS `item_category`
(
    `_id`
    INTEGER
    NOT
    NULL,
    `description`
    TEXT
    NOT
    NULL,
    `active`
    INTEGER
    NOT
    NULL,
    `parent_id`
    INTEGER
    NOT
    NULL,
    `transferred`
    INTEGER,
    PRIMARY
    KEY
(
    `_id`
)
    );
CREATE INDEX IF NOT EXISTS `IDX_item_category_description` ON `item_category` (`description`);
CREATE INDEX IF NOT EXISTS `IDX_item_category_parent_id` ON `item_category` (`parent_id`);

CREATE TABLE IF NOT EXISTS `maintenance_status`
(
    `_id`
    INTEGER
    NOT
    NULL,
    `description`
    TEXT
    NOT
    NULL,
    PRIMARY
    KEY
(
    `_id`
)
    );
CREATE INDEX IF NOT EXISTS `IDX_maintenance_status__id` ON `maintenance_status` (`_id`);
CREATE INDEX IF NOT EXISTS `IDX_maintenance_status_description` ON `maintenance_status` (`description`);

CREATE TABLE IF NOT EXISTS `maintenance_type`
(
    `_id`
    INTEGER
    NOT
    NULL,
    `description`
    TEXT
    NOT
    NULL,
    `active`
    INTEGER
    NOT
    NULL,
    `maintenance_type_group_id`
    INTEGER
    NOT
    NULL,
    PRIMARY
    KEY
(
    `_id`
)
    );
CREATE INDEX IF NOT EXISTS `IDX_maintenance_type_maintenance_type_group_id` ON `maintenance_type` (`maintenance_type_group_id`);
CREATE INDEX IF NOT EXISTS `IDX_maintenance_type_description` ON `maintenance_type` (`description`);

CREATE TABLE IF NOT EXISTS `maintenance_type_group`
(
    `_id`
    INTEGER
    NOT
    NULL,
    `description`
    TEXT
    NOT
    NULL,
    `active`
    INTEGER
    NOT
    NULL,
    PRIMARY
    KEY
(
    `_id`
)
    );
CREATE INDEX IF NOT EXISTS `IDX_maintenance_type_group_description` ON `maintenance_type_group` (`description`);

CREATE TABLE IF NOT EXISTS `route`
(
    `_id`
    INTEGER
    NOT
    NULL,
    `description`
    TEXT
    NOT
    NULL,
    `active`
    INTEGER
    NOT
    NULL,
    PRIMARY
    KEY
(
    `_id`
)
    );
CREATE INDEX IF NOT EXISTS `IDX_route_description` ON `route` (`description`);

CREATE TABLE IF NOT EXISTS `route_composition`
(
    `route_id`
    INTEGER
    NOT
    NULL,
    `data_collection_rule_id`
    INTEGER
    NOT
    NULL,
    `level`
    INTEGER
    NOT
    NULL,
    `position`
    INTEGER
    NOT
    NULL,
    `asset_id`
    INTEGER,
    `warehouse_id`
    INTEGER,
    `warehouse_area_id`
    INTEGER,
    `expression`
    TEXT,
    `true_result`
    INTEGER
    NOT
    NULL,
    `false_result`
    INTEGER
    NOT
    NULL,
    PRIMARY
    KEY
(
    `route_id`,
    `level`,
    `position`
)
    );
CREATE INDEX IF NOT EXISTS `IDX_route_composition_route_id` ON `route_composition` (`route_id`);
CREATE INDEX IF NOT EXISTS `IDX_route_composition_data_collection_rule_id` ON `route_composition` (`data_collection_rule_id`);
CREATE INDEX IF NOT EXISTS `IDX_route_composition_level` ON `route_composition` (`level`);
CREATE INDEX IF NOT EXISTS `IDX_route_composition_position` ON `route_composition` (`position`);
CREATE INDEX IF NOT EXISTS `IDX_route_composition_asset_id` ON `route_composition` (`asset_id`);
CREATE INDEX IF NOT EXISTS `IDX_route_composition_warehouse_id` ON `route_composition` (`warehouse_id`);
CREATE INDEX IF NOT EXISTS `IDX_route_composition_warehouse_area_id` ON `route_composition` (`warehouse_area_id`);

CREATE TABLE IF NOT EXISTS `route_process`
(
    `_id`
    INTEGER
    PRIMARY
    KEY
    AUTOINCREMENT
    NOT
    NULL,
    `user_id`
    INTEGER
    NOT
    NULL,
    `route_id`
    INTEGER
    NOT
    NULL,
    `route_process_id`
    INTEGER
    NOT
    NULL,
    `route_process_date`
    INTEGER
    NOT
    NULL,
    `completed`
    INTEGER
    NOT
    NULL,
    `transferred`
    INTEGER,
    `transferred_date`
    INTEGER
);
CREATE INDEX IF NOT EXISTS `IDX_route_process_user_id` ON `route_process` (`user_id`);
CREATE INDEX IF NOT EXISTS `IDX_route_process_route_id` ON `route_process` (`route_id`);
CREATE INDEX IF NOT EXISTS `IDX_route_process_route_process_id` ON `route_process` (`route_process_id`);

CREATE TABLE IF NOT EXISTS `route_process_content`
(
    `_id`
    INTEGER
    PRIMARY
    KEY
    AUTOINCREMENT
    NOT
    NULL,
    `route_process_id`
    INTEGER
    NOT
    NULL,
    `data_collection_rule_id`
    INTEGER
    NOT
    NULL,
    `level`
    INTEGER
    NOT
    NULL,
    `position`
    INTEGER
    NOT
    NULL,
    `route_process_status_id`
    INTEGER
    NOT
    NULL,
    `data_collection_id`
    INTEGER
);
CREATE INDEX IF NOT EXISTS `IDX_route_process_content_route_process_id` ON `route_process_content` (`route_process_id`);
CREATE INDEX IF NOT EXISTS `IDX_route_process_content_data_collection_rule_id` ON `route_process_content` (`data_collection_rule_id`);
CREATE INDEX IF NOT EXISTS `IDX_route_process_content_level` ON `route_process_content` (`level`);
CREATE INDEX IF NOT EXISTS `IDX_route_process_content_position` ON `route_process_content` (`position`);
CREATE INDEX IF NOT EXISTS `IDX_route_process_content_route_process_status_id` ON `route_process_content` (`route_process_status_id`);
CREATE INDEX IF NOT EXISTS `IDX_route_process_content_data_collection_id` ON `route_process_content` (`data_collection_id`);

CREATE TABLE IF NOT EXISTS `route_process_status`
(
    `_id`
    INTEGER
    NOT
    NULL,
    `description`
    TEXT
    NOT
    NULL,
    PRIMARY
    KEY
(
    `_id`
)
    );
CREATE INDEX IF NOT EXISTS `IDX_route_process_status__id` ON `route_process_status` (`_id`);
CREATE INDEX IF NOT EXISTS `IDX_route_process_status_description` ON `route_process_status` (`description`);

CREATE TABLE IF NOT EXISTS `route_process_steps`
(
    `route_process_id`
    INTEGER
    NOT
    NULL,
    `route_process_content_id`
    INTEGER
    NOT
    NULL,
    `level`
    INTEGER
    NOT
    NULL,
    `position`
    INTEGER
    NOT
    NULL,
    `data_collection_id`
    INTEGER,
    `step`
    INTEGER
    NOT
    NULL,
    PRIMARY
    KEY
(
    `route_process_id`,
    `level`,
    `position`
)
    );
CREATE INDEX IF NOT EXISTS `IDX_route_process_steps_route_process_id` ON `route_process_steps` (`route_process_id`);
CREATE INDEX IF NOT EXISTS `IDX_route_process_steps_route_process_content_id` ON `route_process_steps` (`route_process_content_id`);
CREATE INDEX IF NOT EXISTS `IDX_route_process_steps_level` ON `route_process_steps` (`level`);
CREATE INDEX IF NOT EXISTS `IDX_route_process_steps_position` ON `route_process_steps` (`position`);
CREATE INDEX IF NOT EXISTS `IDX_route_process_steps_data_collection_id` ON `route_process_steps` (`data_collection_id`);
CREATE INDEX IF NOT EXISTS `IDX_route_process_steps_step` ON `route_process_steps` (`step`);

CREATE TABLE IF NOT EXISTS `status`
(
    `_id`
    INTEGER
    NOT
    NULL,
    `description`
    TEXT
    NOT
    NULL,
    PRIMARY
    KEY
(
    `_id`
)
    );
CREATE INDEX IF NOT EXISTS `IDX_status__id` ON `status` (`_id`);
CREATE INDEX IF NOT EXISTS `IDX_status_description` ON `status` (`description`);

CREATE TABLE IF NOT EXISTS `user`
(
    `_id`
    INTEGER
    NOT
    NULL,
    `name`
    TEXT
    NOT
    NULL,
    `external_id`
    TEXT,
    `email`
    TEXT
    NOT
    NULL,
    `active`
    INTEGER
    NOT
    NULL,
    `password`
    TEXT,
    PRIMARY
    KEY
(
    `_id`
)
    );
CREATE INDEX IF NOT EXISTS `IDX_user_name` ON `user` (`name`);
CREATE INDEX IF NOT EXISTS `IDX_user_external_id` ON `user` (`external_id`);
CREATE INDEX IF NOT EXISTS `IDX_user_email` ON `user` (`email`);

CREATE TABLE IF NOT EXISTS `user_permission`
(
    `user_id`
    INTEGER
    NOT
    NULL,
    `permission_id`
    INTEGER
    NOT
    NULL,
    PRIMARY
    KEY
(
    `user_id`,
    `permission_id`
)
    );
CREATE INDEX IF NOT EXISTS `IDX_user_permission_user_id` ON `user_permission` (`user_id`);
CREATE INDEX IF NOT EXISTS `IDX_user_permission_permission_id` ON `user_permission` (`permission_id`);

CREATE TABLE IF NOT EXISTS `user_warehouse_area`
(
    `user_id`
    INTEGER
    NOT
    NULL,
    `warehouse_area_id`
    INTEGER
    NOT
    NULL,
    `see`
    INTEGER
    NOT
    NULL,
    `move`
    INTEGER
    NOT
    NULL,
    `count`
    INTEGER
    NOT
    NULL,
    `check`
    INTEGER
    NOT
    NULL,
    PRIMARY
    KEY
(
    `user_id`,
    `warehouse_area_id`
)
    );
CREATE INDEX IF NOT EXISTS `IDX_user_warehouse_area_user_id` ON `user_warehouse_area` (`user_id`);
CREATE INDEX IF NOT EXISTS `IDX_user_warehouse_area_warehouse_area_id` ON `user_warehouse_area` (`warehouse_area_id`);

CREATE TABLE IF NOT EXISTS `warehouse`
(
    `_id`
    INTEGER
    NOT
    NULL,
    `description`
    TEXT
    NOT
    NULL,
    `active`
    INTEGER
    NOT
    NULL,
    `transferred`
    INTEGER,
    PRIMARY
    KEY
(
    `_id`
)
    );
CREATE INDEX IF NOT EXISTS `IDX_warehouse_description` ON `warehouse` (`description`);

CREATE TABLE IF NOT EXISTS `warehouse_area`
(
    `_id`
    INTEGER
    NOT
    NULL,
    `description`
    TEXT
    NOT
    NULL,
    `active`
    INTEGER
    NOT
    NULL,
    `warehouse_id`
    INTEGER
    NOT
    NULL,
    `transferred`
    INTEGER,
    PRIMARY
    KEY
(
    `_id`
)
    );
CREATE INDEX IF NOT EXISTS `IDX_warehouse_area_description` ON `warehouse_area` (`description`);
CREATE INDEX IF NOT EXISTS `IDX_warehouse_area_warehouse_id` ON `warehouse_area` (`warehouse_id`);

CREATE TABLE IF NOT EXISTS `warehouse_movement`
(
    `_id`
    INTEGER
    PRIMARY
    KEY
    AUTOINCREMENT
    NOT
    NULL,
    `warehouse_movement_id`
    INTEGER
    NOT
    NULL,
    `warehouse_movement_date`
    INTEGER
    NOT
    NULL,
    `obs`
    TEXT,
    `user_id`
    INTEGER
    NOT
    NULL,
    `origin_warehouse_area_id`
    INTEGER
    NOT
    NULL,
    `origin_warehouse_id`
    INTEGER
    NOT
    NULL,
    `transferred_date`
    INTEGER,
    `destination_warehouse_area_id`
    INTEGER
    NOT
    NULL,
    `destination_warehouse_id`
    INTEGER
    NOT
    NULL,
    `completed`
    INTEGER
);
CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_warehouse_movement_id` ON `warehouse_movement` (`warehouse_movement_id`);
CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_user_id` ON `warehouse_movement` (`user_id`);
CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_origin_warehouse_area_id` ON `warehouse_movement` (`origin_warehouse_area_id`);
CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_origin_warehouse_id` ON `warehouse_movement` (`origin_warehouse_id`);
CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_destination_warehouse_area_id` ON `warehouse_movement` (`destination_warehouse_area_id`);
CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_destination_warehouse_id` ON `warehouse_movement` (`destination_warehouse_id`);

CREATE TABLE IF NOT EXISTS `warehouse_movement_content`
(
    `_id`
    INTEGER
    PRIMARY
    KEY
    AUTOINCREMENT
    NOT
    NULL,
    `warehouse_movement_id`
    INTEGER
    NOT
    NULL,
    `asset_id`
    INTEGER
    NOT
    NULL,
    `code`
    TEXT
    NOT
    NULL,
    `qty`
    REAL
);
CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_content_warehouse_movement_id` ON `warehouse_movement_content` (`warehouse_movement_id`);
CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_content_asset_id` ON `warehouse_movement_content` (`asset_id`);
CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_content_code` ON `warehouse_movement_content` (`code`);

CREATE TABLE IF NOT EXISTS room_master_table
(
    id
    INTEGER
    PRIMARY
    KEY,
    identity_hash
    TEXT
);
INSERT
OR
REPLACE
INTO room_master_table (id, identity_hash)
VALUES (42, '486c994e9e261a6da726faa6728eb512');
