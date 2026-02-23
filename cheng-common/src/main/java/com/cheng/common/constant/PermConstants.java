package com.cheng.common.constant;

/**
 * 權限字串常數
 * <p>
 * 集中管理所有 {@code @PreAuthorize} 與前端 {@code v-hasPermi} 使用的權限標識。
 * 使用嵌套 interface 結構，欄位預設為 {@code public static final}，
 * 可直接在 {@code @PreAuthorize} 中以編譯期字串拼接方式引用。
 * <p>
 * 使用方式：
 * <pre>{@code
 * @PreAuthorize("@ss.hasPermi('" + PermConstants.System.User.LIST + "')")
 * }</pre>
 *
 * @author cheng
 */
public final class PermConstants {

    private PermConstants() {
    }

    // ===== System 模組 =====

    public interface System {

        interface User {
            String LIST      = "system:user:list";
            String QUERY     = "system:user:query";
            String ADD       = "system:user:add";
            String EDIT      = "system:user:edit";
            String REMOVE    = "system:user:remove";
            String EXPORT    = "system:user:export";
            String IMPORT    = "system:user:import";
            String RESET_PWD = "system:user:resetPwd";
        }

        interface Role {
            String LIST   = "system:role:list";
            String QUERY  = "system:role:query";
            String ADD    = "system:role:add";
            String EDIT   = "system:role:edit";
            String REMOVE = "system:role:remove";
            String EXPORT = "system:role:export";
        }

        interface Menu {
            String LIST   = "system:menu:list";
            String QUERY  = "system:menu:query";
            String ADD    = "system:menu:add";
            String EDIT   = "system:menu:edit";
            String REMOVE = "system:menu:remove";
        }

        interface Dept {
            String LIST   = "system:dept:list";
            String QUERY  = "system:dept:query";
            String ADD    = "system:dept:add";
            String EDIT   = "system:dept:edit";
            String REMOVE = "system:dept:remove";
        }

        interface Post {
            String LIST   = "system:post:list";
            String QUERY  = "system:post:query";
            String ADD    = "system:post:add";
            String EDIT   = "system:post:edit";
            String REMOVE = "system:post:remove";
            String EXPORT = "system:post:export";
        }

        interface Dict {
            String LIST   = "system:dict:list";
            String QUERY  = "system:dict:query";
            String ADD    = "system:dict:add";
            String EDIT   = "system:dict:edit";
            String REMOVE = "system:dict:remove";
            String EXPORT = "system:dict:export";
        }

        interface Config {
            String LIST   = "system:config:list";
            String QUERY  = "system:config:query";
            String ADD    = "system:config:add";
            String EDIT   = "system:config:edit";
            String REMOVE = "system:config:remove";
            String EXPORT = "system:config:export";
        }

        interface Notice {
            String LIST   = "system:notice:list";
            String QUERY  = "system:notice:query";
            String ADD    = "system:notice:add";
            String EDIT   = "system:notice:edit";
            String REMOVE = "system:notice:remove";
        }

        interface TableConfig {
            String CUSTOMIZE = "system:tableConfig:customize";
            String TEMPLATE  = "system:tableConfig:template";
        }

        interface MaterialAudio {
            String LIST   = "system:material:audio:list";
            String QUERY  = "system:material:audio:query";
            String UPLOAD = "system:material:audio:upload";
            String REMOVE = "system:material:audio:remove";
        }

        interface MaterialVideo {
            String LIST   = "system:material:video:list";
            String QUERY  = "system:material:video:query";
            String UPLOAD = "system:material:video:upload";
            String REMOVE = "system:material:video:remove";
        }

        interface MaterialImage {
            String LIST   = "system:material:image:list";
            String QUERY  = "system:material:image:query";
            String UPLOAD = "system:material:image:upload";
            String REMOVE = "system:material:image:remove";
        }
    }

    // ===== Inventory 模組 =====

    public interface Inventory {

        interface Management {
            String LIST         = "inventory:management:list";
            String QUERY        = "inventory:management:query";
            String ADD          = "inventory:management:add";
            String EDIT         = "inventory:management:edit";
            String REMOVE       = "inventory:management:remove";
            String EXPORT       = "inventory:management:export";
            String IMPORT       = "inventory:management:import";
            String STOCK_IN     = "inventory:management:stockIn";
            String STOCK_OUT    = "inventory:management:stockOut";
            String STOCK_CHECK  = "inventory:management:stockCheck";
            String RESERVE      = "inventory:management:reserve";
            String REFRESH_ISBN = "inventory:management:refreshIsbn";
        }

        interface Item {
            String LIST   = "inventory:item:list";
            String QUERY  = "inventory:item:query";
            String ADD    = "inventory:item:add";
            String EDIT   = "inventory:item:edit";
            String REMOVE = "inventory:item:remove";
            String EXPORT = "inventory:item:export";
            String IMPORT = "inventory:item:import";
            String SCAN   = "inventory:item:scan";
        }

        interface Scan {
            String USE = "inventory:scan:use";
        }

        interface Stock {
            String LIST   = "inventory:stock:list";
            String QUERY  = "inventory:stock:query";
            String ADD    = "inventory:stock:add";
            String EDIT   = "inventory:stock:edit";
            String REMOVE = "inventory:stock:remove";
            String EXPORT = "inventory:stock:export";
            String IN     = "inventory:stock:in";
            String OUT    = "inventory:stock:out";
            String CHECK  = "inventory:stock:check";
        }

        interface StockRecord {
            String LIST   = "inventory:stockRecord:list";
            String QUERY  = "inventory:stockRecord:query";
            String ADD    = "inventory:stockRecord:add";
            String EDIT   = "inventory:stockRecord:edit";
            String REMOVE = "inventory:stockRecord:remove";
            String EXPORT = "inventory:stockRecord:export";
        }

        interface Borrow {
            String LIST    = "inventory:borrow:list";
            String QUERY   = "inventory:borrow:query";
            String ADD     = "inventory:borrow:add";
            String EDIT    = "inventory:borrow:edit";
            String REMOVE  = "inventory:borrow:remove";
            String EXPORT  = "inventory:borrow:export";
            String BORROW  = "inventory:borrow:borrow";
            String RETURN  = "inventory:borrow:return";
            String APPROVE = "inventory:borrow:approve";
            String ALL     = "inventory:borrow:all";
        }

        interface Category {
            String LIST   = "inventory:category:list";
            String QUERY  = "inventory:category:query";
            String ADD    = "inventory:category:add";
            String EDIT   = "inventory:category:edit";
            String REMOVE = "inventory:category:remove";
            String EXPORT = "inventory:category:export";
        }

        interface BookInfo {
            String LIST   = "inventory:bookInfo:list";
            String QUERY  = "inventory:bookInfo:query";
            String ADD    = "inventory:bookInfo:add";
            String EDIT   = "inventory:bookInfo:edit";
            String REMOVE = "inventory:bookInfo:remove";
            String EXPORT = "inventory:bookInfo:export";
        }

        interface Report {
            String VIEW   = "inventory:report:view";
            String EXPORT = "inventory:report:export";
        }
    }

    // ===== Tag 模組 =====

    public interface Tag {

        interface Inventory {
            String LIST       = "tag:inventory:list";
            String QUERY      = "tag:inventory:query";
            String ADD        = "tag:inventory:add";
            String EDIT       = "tag:inventory:edit";
            String REMOVE     = "tag:inventory:remove";
            String BIND_ITEM  = "tag:inventory:bindItem";
            String BIND_QUERY = "tag:inventory:bindQuery";
            String BATCH_BIND = "tag:inventory:batchBind";
            String UNBIND     = "tag:inventory:unbind";
        }

        interface Line {
            String LIST       = "tag:line:list";
            String QUERY      = "tag:line:query";
            String ADD        = "tag:line:add";
            String EDIT       = "tag:line:edit";
            String REMOVE     = "tag:line:remove";
            String BIND_USER  = "tag:line:bindUser";
            String BIND_QUERY = "tag:line:bindQuery";
            String BATCH_BIND = "tag:line:batchBind";
            String UNBIND     = "tag:line:unbind";
        }

        interface GroupInventory {
            String LIST  = "tag:group:inventory:list";
            String QUERY = "tag:group:inventory:query";
            String ADD   = "tag:group:inventory:add";
            String EDIT  = "tag:group:inventory:edit";
            String REMOVE = "tag:group:inventory:remove";
            String CALC  = "tag:group:inventory:calc";
        }

        interface GroupLine {
            String LIST   = "tag:group:line:list";
            String QUERY  = "tag:group:line:query";
            String ADD    = "tag:group:line:add";
            String EDIT   = "tag:group:line:edit";
            String REMOVE = "tag:group:line:remove";
            String CALC   = "tag:group:line:calc";
        }
    }

    // ===== Line 模組 =====

    public interface Line {

        interface User {
            String LIST   = "line:user:list";
            String QUERY  = "line:user:query";
            String BIND   = "line:user:bind";
            String EDIT   = "line:user:edit";
            String REMOVE = "line:user:remove";
            String EXPORT = "line:user:export";
            String IMPORT = "line:user:import";
        }

        interface Message {
            String LIST   = "line:message:list";
            String QUERY  = "line:message:query";
            String SEND   = "line:message:send";
            String REMOVE = "line:message:remove";
            String EXPORT = "line:message:export";
        }

        interface Config {
            String LIST   = "line:config:list";
            String QUERY  = "line:config:query";
            String ADD    = "line:config:add";
            String EDIT   = "line:config:edit";
            String REMOVE = "line:config:remove";
            String EXPORT = "line:config:export";
            String TEST   = "line:config:test";
        }

        interface RichMenu {
            String LIST        = "line:richMenu:list";
            String QUERY       = "line:richMenu:query";
            String ADD         = "line:richMenu:add";
            String EDIT        = "line:richMenu:edit";
            String REMOVE      = "line:richMenu:remove";
            String EXPORT      = "line:richMenu:export";
            String PUBLISH     = "line:richMenu:publish";
            String SET_DEFAULT = "line:richMenu:setDefault";
        }

        interface RichMenuAlias {
            String LIST   = "line:richMenuAlias:list";
            String QUERY  = "line:richMenuAlias:query";
            String ADD    = "line:richMenuAlias:add";
            String EDIT   = "line:richMenuAlias:edit";
            String REMOVE = "line:richMenuAlias:remove";
            String EXPORT = "line:richMenuAlias:export";
            String SYNC   = "line:richMenuAlias:sync";
        }

        interface Template {
            String LIST   = "line:template:list";
            String QUERY  = "line:template:query";
            String ADD    = "line:template:add";
            String EDIT   = "line:template:edit";
            String REMOVE = "line:template:remove";
            String EXPORT = "line:template:export";
            String SEND   = "line:template:send";
        }
    }

    // ===== Shop 模組 =====

    public interface Shop {

        interface Product {
            String LIST   = "shop:product:list";
            String QUERY  = "shop:product:query";
            String ADD    = "shop:product:add";
            String EDIT   = "shop:product:edit";
            String REMOVE = "shop:product:remove";
        }

        interface Order {
            String LIST              = "shop:order:list";
            String QUERY             = "shop:order:query";
            String EDIT              = "shop:order:edit";
            String SHIP              = "shop:order:ship";
            String CANCEL            = "shop:order:cancel";
            String DELIVER           = "shop:order:deliver";
            String COMPLETE          = "shop:order:complete";
            String UPDATE_PAY_STATUS  = "shop:order:updatePayStatus";
            String UPDATE_SHIP_STATUS = "shop:order:updateShipStatus";
            String EXPORT            = "shop:order:export";
        }

        interface Category {
            String LIST   = "shop:category:list";
            String QUERY  = "shop:category:query";
            String ADD    = "shop:category:add";
            String EDIT   = "shop:category:edit";
            String REMOVE = "shop:category:remove";
        }

        interface Banner {
            String LIST   = "shop:banner:list";
            String QUERY  = "shop:banner:query";
            String ADD    = "shop:banner:add";
            String EDIT   = "shop:banner:edit";
            String REMOVE = "shop:banner:remove";
        }

        interface Block {
            String LIST   = "shop:block:list";
            String QUERY  = "shop:block:query";
            String ADD    = "shop:block:add";
            String EDIT   = "shop:block:edit";
            String REMOVE = "shop:block:remove";
        }

        interface Article {
            String LIST   = "shop:article:list";
            String QUERY  = "shop:article:query";
            String ADD    = "shop:article:add";
            String EDIT   = "shop:article:edit";
            String REMOVE = "shop:article:remove";
        }

        interface Gift {
            String LIST   = "shop:gift:list";
            String QUERY  = "shop:gift:query";
            String ADD    = "shop:gift:add";
            String EDIT   = "shop:gift:edit";
            String REMOVE = "shop:gift:remove";
        }

        interface Member {
            String LIST   = "shop:member:list";
            String QUERY  = "shop:member:query";
            String ADD    = "shop:member:add";
            String EDIT   = "shop:member:edit";
            String REMOVE = "shop:member:remove";
            String EXPORT = "shop:member:export";
        }

        interface Cart {
            String QUERY  = "shop:cart:query";
            String ADD    = "shop:cart:add";
            String EDIT   = "shop:cart:edit";
            String REMOVE = "shop:cart:remove";
        }

        interface PaymentCallback {
            String LIST  = "shop:payment:callback:list";
            String QUERY = "shop:payment:callback:query";
        }
    }

    // ===== Monitor 模組 =====

    public interface Monitor {

        interface Online {
            String LIST         = "monitor:online:list";
            String FORCE_LOGOUT = "monitor:online:forceLogout";
        }

        interface Server {
            String LIST = "monitor:server:list";
        }

        interface Cache {
            String LIST = "monitor:cache:list";
        }

        interface Operlog {
            String LIST   = "monitor:operlog:list";
            String QUERY  = "monitor:operlog:query";
            String REMOVE = "monitor:operlog:remove";
            String EXPORT = "monitor:operlog:export";
        }

        interface Logininfor {
            String LIST   = "monitor:logininfor:list";
            String REMOVE = "monitor:logininfor:remove";
            String EXPORT = "monitor:logininfor:export";
            String UNLOCK = "monitor:logininfor:unlock";
        }

        interface Job {
            String LIST          = "monitor:job:list";
            String QUERY         = "monitor:job:query";
            String ADD           = "monitor:job:add";
            String EDIT          = "monitor:job:edit";
            String REMOVE        = "monitor:job:remove";
            String EXPORT        = "monitor:job:export";
            String CHANGE_STATUS = "monitor:job:changeStatus";
        }
    }

    // ===== Tool 模組 =====

    public interface Tool {

        interface Gen {
            String LIST    = "tool:gen:list";
            String QUERY   = "tool:gen:query";
            String IMPORT  = "tool:gen:import";
            String EDIT    = "tool:gen:edit";
            String REMOVE  = "tool:gen:remove";
            String PREVIEW = "tool:gen:preview";
            String CODE    = "tool:gen:code";
        }
    }
}
