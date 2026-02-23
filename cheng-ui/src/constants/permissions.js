/**
 * 權限字串常數
 *
 * 集中管理所有前端 v-hasPermi / checkPermi / auth.hasPermi 使用的權限標識。
 * 使用 SCREAMING_SNAKE_CASE 扁平 named export，方便 tree-shaking 與 import。
 *
 * 命名規則：{MODULE}_{ENTITY}_{ACTION}
 * 對應後端 PermConstants.java
 */

// ===== System =====

// System > User
export const SYSTEM_USER_LIST      = 'system:user:list'
export const SYSTEM_USER_QUERY     = 'system:user:query'
export const SYSTEM_USER_ADD       = 'system:user:add'
export const SYSTEM_USER_EDIT      = 'system:user:edit'
export const SYSTEM_USER_REMOVE    = 'system:user:remove'
export const SYSTEM_USER_EXPORT    = 'system:user:export'
export const SYSTEM_USER_IMPORT    = 'system:user:import'
export const SYSTEM_USER_RESET_PWD = 'system:user:resetPwd'

// System > Role
export const SYSTEM_ROLE_LIST   = 'system:role:list'
export const SYSTEM_ROLE_QUERY  = 'system:role:query'
export const SYSTEM_ROLE_ADD    = 'system:role:add'
export const SYSTEM_ROLE_EDIT   = 'system:role:edit'
export const SYSTEM_ROLE_REMOVE = 'system:role:remove'
export const SYSTEM_ROLE_EXPORT = 'system:role:export'

// System > Menu
export const SYSTEM_MENU_LIST   = 'system:menu:list'
export const SYSTEM_MENU_QUERY  = 'system:menu:query'
export const SYSTEM_MENU_ADD    = 'system:menu:add'
export const SYSTEM_MENU_EDIT   = 'system:menu:edit'
export const SYSTEM_MENU_REMOVE = 'system:menu:remove'

// System > Dept
export const SYSTEM_DEPT_LIST   = 'system:dept:list'
export const SYSTEM_DEPT_QUERY  = 'system:dept:query'
export const SYSTEM_DEPT_ADD    = 'system:dept:add'
export const SYSTEM_DEPT_EDIT   = 'system:dept:edit'
export const SYSTEM_DEPT_REMOVE = 'system:dept:remove'

// System > Post
export const SYSTEM_POST_LIST   = 'system:post:list'
export const SYSTEM_POST_QUERY  = 'system:post:query'
export const SYSTEM_POST_ADD    = 'system:post:add'
export const SYSTEM_POST_EDIT   = 'system:post:edit'
export const SYSTEM_POST_REMOVE = 'system:post:remove'
export const SYSTEM_POST_EXPORT = 'system:post:export'

// System > Dict
export const SYSTEM_DICT_LIST   = 'system:dict:list'
export const SYSTEM_DICT_QUERY  = 'system:dict:query'
export const SYSTEM_DICT_ADD    = 'system:dict:add'
export const SYSTEM_DICT_EDIT   = 'system:dict:edit'
export const SYSTEM_DICT_REMOVE = 'system:dict:remove'
export const SYSTEM_DICT_EXPORT = 'system:dict:export'

// System > Config
export const SYSTEM_CONFIG_LIST   = 'system:config:list'
export const SYSTEM_CONFIG_QUERY  = 'system:config:query'
export const SYSTEM_CONFIG_ADD    = 'system:config:add'
export const SYSTEM_CONFIG_EDIT   = 'system:config:edit'
export const SYSTEM_CONFIG_REMOVE = 'system:config:remove'
export const SYSTEM_CONFIG_EXPORT = 'system:config:export'

// System > Notice
export const SYSTEM_NOTICE_LIST   = 'system:notice:list'
export const SYSTEM_NOTICE_QUERY  = 'system:notice:query'
export const SYSTEM_NOTICE_ADD    = 'system:notice:add'
export const SYSTEM_NOTICE_EDIT   = 'system:notice:edit'
export const SYSTEM_NOTICE_REMOVE = 'system:notice:remove'

// System > TableConfig
export const SYSTEM_TABLE_CONFIG_CUSTOMIZE = 'system:tableConfig:customize'
export const SYSTEM_TABLE_CONFIG_TEMPLATE  = 'system:tableConfig:template'

// System > Material (Audio / Video / Image)
export const SYSTEM_MATERIAL_AUDIO_LIST   = 'system:material:audio:list'
export const SYSTEM_MATERIAL_AUDIO_QUERY  = 'system:material:audio:query'
export const SYSTEM_MATERIAL_AUDIO_UPLOAD = 'system:material:audio:upload'
export const SYSTEM_MATERIAL_AUDIO_REMOVE = 'system:material:audio:remove'

export const SYSTEM_MATERIAL_VIDEO_LIST   = 'system:material:video:list'
export const SYSTEM_MATERIAL_VIDEO_QUERY  = 'system:material:video:query'
export const SYSTEM_MATERIAL_VIDEO_UPLOAD = 'system:material:video:upload'
export const SYSTEM_MATERIAL_VIDEO_REMOVE = 'system:material:video:remove'

export const SYSTEM_MATERIAL_IMAGE_LIST   = 'system:material:image:list'
export const SYSTEM_MATERIAL_IMAGE_QUERY  = 'system:material:image:query'
export const SYSTEM_MATERIAL_IMAGE_UPLOAD = 'system:material:image:upload'
export const SYSTEM_MATERIAL_IMAGE_REMOVE = 'system:material:image:remove'

// ===== Inventory =====

// Inventory > Management
export const INVENTORY_MANAGEMENT_LIST         = 'inventory:management:list'
export const INVENTORY_MANAGEMENT_QUERY        = 'inventory:management:query'
export const INVENTORY_MANAGEMENT_ADD          = 'inventory:management:add'
export const INVENTORY_MANAGEMENT_EDIT         = 'inventory:management:edit'
export const INVENTORY_MANAGEMENT_REMOVE       = 'inventory:management:remove'
export const INVENTORY_MANAGEMENT_EXPORT       = 'inventory:management:export'
export const INVENTORY_MANAGEMENT_IMPORT       = 'inventory:management:import'
export const INVENTORY_MANAGEMENT_STOCK_IN     = 'inventory:management:stockIn'
export const INVENTORY_MANAGEMENT_STOCK_OUT    = 'inventory:management:stockOut'
export const INVENTORY_MANAGEMENT_STOCK_CHECK  = 'inventory:management:stockCheck'
export const INVENTORY_MANAGEMENT_RESERVE      = 'inventory:management:reserve'
export const INVENTORY_MANAGEMENT_REFRESH_ISBN = 'inventory:management:refreshIsbn'

// Inventory > Item
export const INVENTORY_ITEM_LIST   = 'inventory:item:list'
export const INVENTORY_ITEM_QUERY  = 'inventory:item:query'
export const INVENTORY_ITEM_ADD    = 'inventory:item:add'
export const INVENTORY_ITEM_EDIT   = 'inventory:item:edit'
export const INVENTORY_ITEM_REMOVE = 'inventory:item:remove'
export const INVENTORY_ITEM_EXPORT = 'inventory:item:export'
export const INVENTORY_ITEM_IMPORT = 'inventory:item:import'
export const INVENTORY_ITEM_SCAN   = 'inventory:item:scan'

// Inventory > Scan
export const INVENTORY_SCAN_USE = 'inventory:scan:use'

// Inventory > Stock
export const INVENTORY_STOCK_LIST   = 'inventory:stock:list'
export const INVENTORY_STOCK_QUERY  = 'inventory:stock:query'
export const INVENTORY_STOCK_ADD    = 'inventory:stock:add'
export const INVENTORY_STOCK_EDIT   = 'inventory:stock:edit'
export const INVENTORY_STOCK_REMOVE = 'inventory:stock:remove'
export const INVENTORY_STOCK_EXPORT = 'inventory:stock:export'
export const INVENTORY_STOCK_IN     = 'inventory:stock:in'
export const INVENTORY_STOCK_OUT    = 'inventory:stock:out'
export const INVENTORY_STOCK_CHECK  = 'inventory:stock:check'

// Inventory > StockRecord
export const INVENTORY_STOCK_RECORD_LIST   = 'inventory:stockRecord:list'
export const INVENTORY_STOCK_RECORD_QUERY  = 'inventory:stockRecord:query'
export const INVENTORY_STOCK_RECORD_ADD    = 'inventory:stockRecord:add'
export const INVENTORY_STOCK_RECORD_EDIT   = 'inventory:stockRecord:edit'
export const INVENTORY_STOCK_RECORD_REMOVE = 'inventory:stockRecord:remove'
export const INVENTORY_STOCK_RECORD_EXPORT = 'inventory:stockRecord:export'

// Inventory > Borrow
export const INVENTORY_BORROW_LIST    = 'inventory:borrow:list'
export const INVENTORY_BORROW_QUERY   = 'inventory:borrow:query'
export const INVENTORY_BORROW_ADD     = 'inventory:borrow:add'
export const INVENTORY_BORROW_EDIT    = 'inventory:borrow:edit'
export const INVENTORY_BORROW_REMOVE  = 'inventory:borrow:remove'
export const INVENTORY_BORROW_EXPORT  = 'inventory:borrow:export'
export const INVENTORY_BORROW_BORROW  = 'inventory:borrow:borrow'
export const INVENTORY_BORROW_RETURN  = 'inventory:borrow:return'
export const INVENTORY_BORROW_APPROVE = 'inventory:borrow:approve'
export const INVENTORY_BORROW_ALL     = 'inventory:borrow:all'

// Inventory > Category
export const INVENTORY_CATEGORY_LIST   = 'inventory:category:list'
export const INVENTORY_CATEGORY_QUERY  = 'inventory:category:query'
export const INVENTORY_CATEGORY_ADD    = 'inventory:category:add'
export const INVENTORY_CATEGORY_EDIT   = 'inventory:category:edit'
export const INVENTORY_CATEGORY_REMOVE = 'inventory:category:remove'
export const INVENTORY_CATEGORY_EXPORT = 'inventory:category:export'

// Inventory > BookInfo
export const INVENTORY_BOOK_INFO_LIST   = 'inventory:bookInfo:list'
export const INVENTORY_BOOK_INFO_QUERY  = 'inventory:bookInfo:query'
export const INVENTORY_BOOK_INFO_ADD    = 'inventory:bookInfo:add'
export const INVENTORY_BOOK_INFO_EDIT   = 'inventory:bookInfo:edit'
export const INVENTORY_BOOK_INFO_REMOVE = 'inventory:bookInfo:remove'
export const INVENTORY_BOOK_INFO_EXPORT = 'inventory:bookInfo:export'

// Inventory > Report
export const INVENTORY_REPORT_VIEW   = 'inventory:report:view'
export const INVENTORY_REPORT_EXPORT = 'inventory:report:export'

// ===== Tag =====

// Tag > Inventory
export const TAG_INVENTORY_LIST       = 'tag:inventory:list'
export const TAG_INVENTORY_QUERY      = 'tag:inventory:query'
export const TAG_INVENTORY_ADD        = 'tag:inventory:add'
export const TAG_INVENTORY_EDIT       = 'tag:inventory:edit'
export const TAG_INVENTORY_REMOVE     = 'tag:inventory:remove'
export const TAG_INVENTORY_BIND_ITEM  = 'tag:inventory:bindItem'
export const TAG_INVENTORY_BIND_QUERY = 'tag:inventory:bindQuery'
export const TAG_INVENTORY_BATCH_BIND = 'tag:inventory:batchBind'
export const TAG_INVENTORY_UNBIND     = 'tag:inventory:unbind'

// Tag > Line
export const TAG_LINE_LIST       = 'tag:line:list'
export const TAG_LINE_QUERY      = 'tag:line:query'
export const TAG_LINE_ADD        = 'tag:line:add'
export const TAG_LINE_EDIT       = 'tag:line:edit'
export const TAG_LINE_REMOVE     = 'tag:line:remove'
export const TAG_LINE_BIND_USER  = 'tag:line:bindUser'
export const TAG_LINE_BIND_QUERY = 'tag:line:bindQuery'
export const TAG_LINE_BATCH_BIND = 'tag:line:batchBind'
export const TAG_LINE_UNBIND     = 'tag:line:unbind'

// Tag > Group > Inventory
export const TAG_GROUP_INVENTORY_LIST   = 'tag:group:inventory:list'
export const TAG_GROUP_INVENTORY_QUERY  = 'tag:group:inventory:query'
export const TAG_GROUP_INVENTORY_ADD    = 'tag:group:inventory:add'
export const TAG_GROUP_INVENTORY_EDIT   = 'tag:group:inventory:edit'
export const TAG_GROUP_INVENTORY_REMOVE = 'tag:group:inventory:remove'
export const TAG_GROUP_INVENTORY_CALC   = 'tag:group:inventory:calc'

// Tag > Group > Line
export const TAG_GROUP_LINE_LIST   = 'tag:group:line:list'
export const TAG_GROUP_LINE_QUERY  = 'tag:group:line:query'
export const TAG_GROUP_LINE_ADD    = 'tag:group:line:add'
export const TAG_GROUP_LINE_EDIT   = 'tag:group:line:edit'
export const TAG_GROUP_LINE_REMOVE = 'tag:group:line:remove'
export const TAG_GROUP_LINE_CALC   = 'tag:group:line:calc'

// ===== Line =====

// Line > User
export const LINE_USER_LIST   = 'line:user:list'
export const LINE_USER_QUERY  = 'line:user:query'
export const LINE_USER_BIND   = 'line:user:bind'
export const LINE_USER_EDIT   = 'line:user:edit'
export const LINE_USER_REMOVE = 'line:user:remove'
export const LINE_USER_EXPORT = 'line:user:export'
export const LINE_USER_IMPORT = 'line:user:import'

// Line > Message
export const LINE_MESSAGE_LIST   = 'line:message:list'
export const LINE_MESSAGE_QUERY  = 'line:message:query'
export const LINE_MESSAGE_SEND   = 'line:message:send'
export const LINE_MESSAGE_REMOVE = 'line:message:remove'
export const LINE_MESSAGE_EXPORT = 'line:message:export'

// Line > Config
export const LINE_CONFIG_LIST   = 'line:config:list'
export const LINE_CONFIG_QUERY  = 'line:config:query'
export const LINE_CONFIG_ADD    = 'line:config:add'
export const LINE_CONFIG_EDIT   = 'line:config:edit'
export const LINE_CONFIG_REMOVE = 'line:config:remove'
export const LINE_CONFIG_EXPORT = 'line:config:export'
export const LINE_CONFIG_TEST   = 'line:config:test'

// Line > RichMenu
export const LINE_RICH_MENU_LIST        = 'line:richMenu:list'
export const LINE_RICH_MENU_QUERY       = 'line:richMenu:query'
export const LINE_RICH_MENU_ADD         = 'line:richMenu:add'
export const LINE_RICH_MENU_EDIT        = 'line:richMenu:edit'
export const LINE_RICH_MENU_REMOVE      = 'line:richMenu:remove'
export const LINE_RICH_MENU_EXPORT      = 'line:richMenu:export'
export const LINE_RICH_MENU_PUBLISH     = 'line:richMenu:publish'
export const LINE_RICH_MENU_SET_DEFAULT = 'line:richMenu:setDefault'

// Line > RichMenuAlias
export const LINE_RICH_MENU_ALIAS_LIST   = 'line:richMenuAlias:list'
export const LINE_RICH_MENU_ALIAS_QUERY  = 'line:richMenuAlias:query'
export const LINE_RICH_MENU_ALIAS_ADD    = 'line:richMenuAlias:add'
export const LINE_RICH_MENU_ALIAS_EDIT   = 'line:richMenuAlias:edit'
export const LINE_RICH_MENU_ALIAS_REMOVE = 'line:richMenuAlias:remove'
export const LINE_RICH_MENU_ALIAS_EXPORT = 'line:richMenuAlias:export'
export const LINE_RICH_MENU_ALIAS_SYNC   = 'line:richMenuAlias:sync'

// Line > Template
export const LINE_TEMPLATE_LIST   = 'line:template:list'
export const LINE_TEMPLATE_QUERY  = 'line:template:query'
export const LINE_TEMPLATE_ADD    = 'line:template:add'
export const LINE_TEMPLATE_EDIT   = 'line:template:edit'
export const LINE_TEMPLATE_REMOVE = 'line:template:remove'
export const LINE_TEMPLATE_EXPORT = 'line:template:export'
export const LINE_TEMPLATE_SEND   = 'line:template:send'

// ===== Shop =====

// Shop > Product
export const SHOP_PRODUCT_LIST   = 'shop:product:list'
export const SHOP_PRODUCT_QUERY  = 'shop:product:query'
export const SHOP_PRODUCT_ADD    = 'shop:product:add'
export const SHOP_PRODUCT_EDIT   = 'shop:product:edit'
export const SHOP_PRODUCT_REMOVE = 'shop:product:remove'

// Shop > Order
export const SHOP_ORDER_LIST               = 'shop:order:list'
export const SHOP_ORDER_QUERY              = 'shop:order:query'
export const SHOP_ORDER_EDIT               = 'shop:order:edit'
export const SHOP_ORDER_SHIP               = 'shop:order:ship'
export const SHOP_ORDER_CANCEL             = 'shop:order:cancel'
export const SHOP_ORDER_DELIVER            = 'shop:order:deliver'
export const SHOP_ORDER_COMPLETE           = 'shop:order:complete'
export const SHOP_ORDER_UPDATE_PAY_STATUS  = 'shop:order:updatePayStatus'
export const SHOP_ORDER_UPDATE_SHIP_STATUS = 'shop:order:updateShipStatus'
export const SHOP_ORDER_EXPORT             = 'shop:order:export'

// Shop > Category
export const SHOP_CATEGORY_LIST   = 'shop:category:list'
export const SHOP_CATEGORY_QUERY  = 'shop:category:query'
export const SHOP_CATEGORY_ADD    = 'shop:category:add'
export const SHOP_CATEGORY_EDIT   = 'shop:category:edit'
export const SHOP_CATEGORY_REMOVE = 'shop:category:remove'

// Shop > Banner
export const SHOP_BANNER_LIST   = 'shop:banner:list'
export const SHOP_BANNER_QUERY  = 'shop:banner:query'
export const SHOP_BANNER_ADD    = 'shop:banner:add'
export const SHOP_BANNER_EDIT   = 'shop:banner:edit'
export const SHOP_BANNER_REMOVE = 'shop:banner:remove'

// Shop > Block
export const SHOP_BLOCK_LIST   = 'shop:block:list'
export const SHOP_BLOCK_QUERY  = 'shop:block:query'
export const SHOP_BLOCK_ADD    = 'shop:block:add'
export const SHOP_BLOCK_EDIT   = 'shop:block:edit'
export const SHOP_BLOCK_REMOVE = 'shop:block:remove'

// Shop > Article
export const SHOP_ARTICLE_LIST   = 'shop:article:list'
export const SHOP_ARTICLE_QUERY  = 'shop:article:query'
export const SHOP_ARTICLE_ADD    = 'shop:article:add'
export const SHOP_ARTICLE_EDIT   = 'shop:article:edit'
export const SHOP_ARTICLE_REMOVE = 'shop:article:remove'

// Shop > Gift
export const SHOP_GIFT_LIST   = 'shop:gift:list'
export const SHOP_GIFT_QUERY  = 'shop:gift:query'
export const SHOP_GIFT_ADD    = 'shop:gift:add'
export const SHOP_GIFT_EDIT   = 'shop:gift:edit'
export const SHOP_GIFT_REMOVE = 'shop:gift:remove'

// Shop > Member
export const SHOP_MEMBER_LIST   = 'shop:member:list'
export const SHOP_MEMBER_QUERY  = 'shop:member:query'
export const SHOP_MEMBER_ADD    = 'shop:member:add'
export const SHOP_MEMBER_EDIT   = 'shop:member:edit'
export const SHOP_MEMBER_REMOVE = 'shop:member:remove'
export const SHOP_MEMBER_EXPORT = 'shop:member:export'

// Shop > Cart
export const SHOP_CART_QUERY  = 'shop:cart:query'
export const SHOP_CART_ADD    = 'shop:cart:add'
export const SHOP_CART_EDIT   = 'shop:cart:edit'
export const SHOP_CART_REMOVE = 'shop:cart:remove'

// Shop > PaymentCallback
export const SHOP_PAYMENT_CALLBACK_LIST  = 'shop:payment:callback:list'
export const SHOP_PAYMENT_CALLBACK_QUERY = 'shop:payment:callback:query'

// ===== Monitor =====

// Monitor > Online
export const MONITOR_ONLINE_LIST         = 'monitor:online:list'
export const MONITOR_ONLINE_FORCE_LOGOUT = 'monitor:online:forceLogout'

// Monitor > Server
export const MONITOR_SERVER_LIST = 'monitor:server:list'

// Monitor > Cache
export const MONITOR_CACHE_LIST = 'monitor:cache:list'

// Monitor > Operlog
export const MONITOR_OPERLOG_LIST   = 'monitor:operlog:list'
export const MONITOR_OPERLOG_QUERY  = 'monitor:operlog:query'
export const MONITOR_OPERLOG_REMOVE = 'monitor:operlog:remove'
export const MONITOR_OPERLOG_EXPORT = 'monitor:operlog:export'

// Monitor > Logininfor
export const MONITOR_LOGININFOR_LIST   = 'monitor:logininfor:list'
export const MONITOR_LOGININFOR_REMOVE = 'monitor:logininfor:remove'
export const MONITOR_LOGININFOR_EXPORT = 'monitor:logininfor:export'
export const MONITOR_LOGININFOR_UNLOCK = 'monitor:logininfor:unlock'

// Monitor > Job
export const MONITOR_JOB_LIST          = 'monitor:job:list'
export const MONITOR_JOB_QUERY         = 'monitor:job:query'
export const MONITOR_JOB_ADD           = 'monitor:job:add'
export const MONITOR_JOB_EDIT          = 'monitor:job:edit'
export const MONITOR_JOB_REMOVE        = 'monitor:job:remove'
export const MONITOR_JOB_EXPORT        = 'monitor:job:export'
export const MONITOR_JOB_CHANGE_STATUS = 'monitor:job:changeStatus'

// ===== Tool =====

// Tool > Gen
export const TOOL_GEN_LIST    = 'tool:gen:list'
export const TOOL_GEN_QUERY   = 'tool:gen:query'
export const TOOL_GEN_IMPORT  = 'tool:gen:import'
export const TOOL_GEN_EDIT    = 'tool:gen:edit'
export const TOOL_GEN_REMOVE  = 'tool:gen:remove'
export const TOOL_GEN_PREVIEW = 'tool:gen:preview'
export const TOOL_GEN_CODE    = 'tool:gen:code'
