## ADDED Requirements

### Requirement: Resolve targets by individual tags
系統 SHALL 接受一組 tagIds，查詢 `line_user_tag_relation` 取得所有貼標使用者的 lineUserId，取聯集並去重後回傳。

#### Scenario: Single tag with multiple users
- **WHEN** 呼叫 resolveTargets(tagIds=[1], tagGroupIds=[]) 且 tag 1 有 100 位使用者
- **THEN** 回傳包含 100 個不重複 lineUserId 的 Set

#### Scenario: Multiple tags with overlapping users
- **WHEN** 呼叫 resolveTargets(tagIds=[1,2], tagGroupIds=[]) 且 tag 1 有 50 人、tag 2 有 30 人、其中 10 人同時有兩個標籤
- **THEN** 回傳 70 個不重複 lineUserId（50 + 30 - 10）

#### Scenario: Tag with no users
- **WHEN** 呼叫 resolveTargets(tagIds=[99], tagGroupIds=[]) 且 tag 99 無任何使用者
- **THEN** 回傳空 Set

### Requirement: Resolve targets by tag groups
系統 SHALL 接受一組 tagGroupIds，根據 `sys_tag_group` 的 calcMode 和 `sys_tag_group_detail` 的 AND/OR 運算子解析目標使用者。

#### Scenario: Tag group with OR logic
- **WHEN** 呼叫 resolveTargets(tagIds=[], tagGroupIds=[1]) 且群組 1 的 calcMode=OR_OF_AND，包含 tag A OR tag B
- **THEN** 回傳 tag A 和 tag B 使用者的聯集

#### Scenario: Tag group with AND logic
- **WHEN** 呼叫 resolveTargets(tagIds=[], tagGroupIds=[1]) 且群組 1 包含 tag A AND tag B
- **THEN** 只回傳同時擁有 tag A 和 tag B 的使用者

### Requirement: Combine individual tags and tag groups
系統 SHALL 支援同時傳入 tagIds 和 tagGroupIds，將兩者的結果取聯集。

#### Scenario: Tags plus tag group combined
- **WHEN** 呼叫 resolveTargets(tagIds=[1], tagGroupIds=[2])
- **THEN** 回傳 tag 1 使用者與群組 2 解析使用者的聯集（去重）

### Requirement: Return user count preview
系統 SHALL 提供預覽 API，僅回傳預計人數而不觸發發送，供前端顯示「預計發送 N 人」。

#### Scenario: Preview count before sending
- **WHEN** 呼叫 GET /line/message/tag/preview?tagIds=1,2&tagGroupIds=3
- **THEN** 回傳 JSON `{ "count": 312, "tagDetails": [{"tagId":1,"name":"VIP","count":234}, ...] }`
