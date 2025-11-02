#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
全專案 Mapper 介面與 XML 實作驗證工具
掃描所有模組，檢查 Mapper 介面方法是否都有對應的 XML 實作
"""

import re
import os
from pathlib import Path

class Color:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    CYAN = '\033[96m'
    MAGENTA = '\033[95m'
    BOLD = '\033[1m'
    END = '\033[0m'

def has_annotation_implementation(content, method_name):
    """檢查方法是否使用註解實作（@Select, @Insert, @Update, @Delete等）"""
    # 找到方法定義的位置
    method_pattern = r'\s+' + re.escape(method_name) + r'\s*\('
    match = re.search(method_pattern, content)
    if not match:
        return False
    
    # 檢查方法定義前的幾行是否有 MyBatis 註解
    # 增加搜尋範圍以處理大型 SQL 註解
    start_pos = max(0, match.start() - 3000)  # 往前看3000個字元（足夠大的 SQL）
    before_method = content[start_pos:match.start()]
    
    # 檢查常見的 MyBatis 註解
    annotations = ['@Select', '@Insert', '@Update', '@Delete', '@SelectProvider', '@InsertProvider', '@UpdateProvider', '@DeleteProvider', '@Results', '@Options']
    for annotation in annotations:
        if annotation in before_method:
            # 再次確認：檢查註解和方法之間沒有其他方法定義
            # 避免誤判前一個方法的註解
            text_between = before_method[before_method.rfind(annotation):]
            # 如果中間有分號（方法結束），則不是當前方法的註解
            if ';' in text_between and not text_between.strip().endswith(';'):
                # 有其他方法的分號，跳過
                continue
            return True
    
    return False

def extract_methods_from_mapper(file_path):
    """從 Mapper 介面檔案中提取需要 XML 實作的方法名稱"""
    methods = []
    annotated_methods = []
    
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
        
    # 匹配介面方法定義
    pattern = r'^\s*(?:public\s+)?(?:int|void|String|Long|Integer|Boolean|List<[^>]+>|Map<[^>]+,[^>]+>|[A-Z][a-zA-Z0-9]*)\s+([a-zA-Z_][a-zA-Z0-9_]*)\s*\([^)]*\)\s*;'
    
    for line in content.split('\n'):
        # 跳過註解
        if line.strip().startswith('*') or line.strip().startswith('//'):
            continue
        
        match = re.match(pattern, line)
        if match:
            method_name = match.group(1)
            
            # 檢查是否使用註解實作
            if has_annotation_implementation(content, method_name):
                annotated_methods.append(method_name)
            else:
                methods.append(method_name)
    
    return methods, annotated_methods

def check_method_in_xml(xml_path, method_name):
    """檢查方法是否在 XML 中有實作"""
    with open(xml_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 搜尋 id="methodName" 或 id='methodName'
    pattern = r'id=["\']' + re.escape(method_name) + r'["\']'
    return re.search(pattern, content) is not None

def find_xml_file(xml_base_dir, mapper_name):
    """遞迴搜尋 XML 檔案"""
    xml_filename = f"{mapper_name}.xml"
    
    # 先檢查直接路徑
    direct_path = xml_base_dir / xml_filename
    if direct_path.exists():
        return direct_path
    
    # 遞迴搜尋所有子目錄
    for root, dirs, files in os.walk(xml_base_dir):
        if xml_filename in files:
            return Path(root) / xml_filename
    
    return None

def verify_mapper(mapper_name, mapper_dir, xml_base_dir, module_name):
    """驗證單個 Mapper"""
    mapper_file = mapper_dir / f"{mapper_name}.java"
    
    if not mapper_file.exists():
        return None, None, None
    
    # 遞迴搜尋 XML 檔案
    xml_file = find_xml_file(xml_base_dir, mapper_name)
    
    if xml_file is None:
        return mapper_name, [], f"找不到 XML 檔案"
    
    # 提取方法
    xml_required_methods, annotated_methods = extract_methods_from_mapper(mapper_file)
    
    total_methods = len(xml_required_methods) + len(annotated_methods)
    
    if total_methods == 0:
        return mapper_name, [], "無法提取方法"
    
    # 檢查需要 XML 實作的方法
    missing_methods = []
    for method in xml_required_methods:
        if not check_method_in_xml(xml_file, method):
            missing_methods.append(method)
    
    # 回傳時包含註解實作的方法數量資訊
    result_info = {
        'total': total_methods,
        'xml_required': len(xml_required_methods),
        'annotated': len(annotated_methods),
        'missing': missing_methods
    }
    
    return mapper_name, result_info, missing_methods

def find_mapper_modules(project_root):
    """尋找專案中所有包含 Mapper 的模組"""
    modules = []
    
    # 掃描專案根目錄下的所有子目錄
    for item in project_root.iterdir():
        if not item.is_dir():
            continue
        
        # 跳過特定目錄
        if item.name.startswith('.') or item.name in ['target', 'node_modules', 'dist', 'docs']:
            continue
        
        # 檢查是否有 Mapper 目錄
        mapper_dir = item / "src/main/java"
        xml_dir = item / "src/main/resources/mapper"
        
        if mapper_dir.exists() and xml_dir.exists():
            # 遞迴查找 mapper 目錄
            mapper_packages = []
            for root, dirs, files in os.walk(mapper_dir):
                if 'mapper' in Path(root).name.lower():
                    mapper_packages.append(Path(root))
            
            if mapper_packages:
                modules.append({
                    'name': item.name,
                    'mapper_dirs': mapper_packages,
                    'xml_dir': xml_dir
                })
    
    return modules

def main():
    # 取得專案根目錄
    # 腳本在 cheng.deploy/ 目錄下，專案根目錄是上一層
    script_dir = Path(__file__).parent
    project_root = script_dir.parent
    
    print("=" * 70)
    print(f"{Color.BOLD}{Color.CYAN}全專案 Mapper 介面與 XML 實作驗證工具{Color.END}")
    print("=" * 70)
    print()
    
    # 尋找所有包含 Mapper 的模組
    modules = find_mapper_modules(project_root)
    
    if not modules:
        print(f"{Color.YELLOW}⚠️  找不到任何包含 Mapper 的模組{Color.END}")
        return 1
    
    print(f"{Color.BLUE}發現 {len(modules)} 個模組包含 Mapper：{Color.END}")
    for module in modules:
        print(f"  - {module['name']}")
    print()
    
    # 統計資料
    total_modules = 0
    passed_modules = 0
    total_mappers = 0
    total_methods = 0
    total_missing = 0
    module_results = []
    
    # 驗證每個模組
    for module in modules:
        module_name = module['name']
        xml_dir = module['xml_dir']
        
        print(f"\n{Color.MAGENTA}{'=' * 70}{Color.END}")
        print(f"{Color.BOLD}{Color.MAGENTA}模組：{module_name}{Color.END}")
        print(f"{Color.MAGENTA}{'=' * 70}{Color.END}\n")
        
        module_passed = True
        module_mapper_count = 0
        module_method_count = 0
        module_missing_count = 0
        
        # 掃描每個 mapper 目錄
        for mapper_dir in module['mapper_dirs']:
            mapper_files = list(mapper_dir.glob("*.java"))
            
            if not mapper_files:
                continue
            
            for mapper_file in sorted(mapper_files):
                mapper_name = mapper_file.stem
                
                # 跳過非 Mapper 檔案
                if not mapper_name.endswith('Mapper'):
                    continue
                
                module_mapper_count += 1
                total_mappers += 1
                
                mapper_name_str, result_info, missing = verify_mapper(
                    mapper_name, mapper_dir, xml_dir, module_name
                )
                
                if mapper_name_str is None:
                    continue
                
                print(f"{Color.BLUE}檢查 {mapper_name}...{Color.END}")
                print(f"  Java: {mapper_file}")
                
                if isinstance(result_info, str):
                    # 錯誤訊息
                    print(f"  {Color.RED}❌ {result_info}{Color.END}")
                    module_passed = False
                    print()
                    continue
                
                # 取得統計資訊
                total_method_count = result_info['total']
                xml_required_count = result_info['xml_required']
                annotated_count = result_info['annotated']
                missing_count = len(missing)
                
                module_method_count += total_method_count
                total_methods += total_method_count
                module_missing_count += missing_count
                total_missing += missing_count
                
                # 顯示詳細資訊
                if annotated_count > 0:
                    print(f"  方法：{total_method_count} 個（XML實作：{xml_required_count}，註解實作：{annotated_count}）")
                
                if missing_count > 0:
                    print(f"  {Color.RED}❌ 缺少 {missing_count} 個 XML 實作：{Color.END}")
                    for method in missing:
                        print(f"     - {method}")
                    module_passed = False
                else:
                    if annotated_count > 0:
                        print(f"  {Color.GREEN}✅ 所有 {xml_required_count} 個需要 XML 的方法都有對應實作{Color.END}")
                    else:
                        print(f"  {Color.GREEN}✅ 所有 {total_method_count} 個方法都有對應的 XML 實作{Color.END}")
                
                print()
        
        total_modules += 1
        if module_passed:
            passed_modules += 1
        
        # 儲存模組結果
        module_results.append({
            'name': module_name,
            'passed': module_passed,
            'mappers': module_mapper_count,
            'methods': module_method_count,
            'missing': module_missing_count
        })
    
    # 顯示總結
    print("\n" + "=" * 70)
    print(f"{Color.BOLD}{Color.CYAN}驗證總結{Color.END}")
    print("=" * 70)
    print()
    
    # 模組統計表格
    print(f"{Color.BOLD}模組統計：{Color.END}\n")
    print(f"{'模組名稱':<25} {'Mapper數':<10} {'方法數':<10} {'缺失數':<10} {'狀態':<10}")
    print("-" * 70)
    
    for result in module_results:
        status = f"{Color.GREEN}✅ 通過{Color.END}" if result['passed'] else f"{Color.RED}❌ 失敗{Color.END}"
        missing_str = str(result['missing']) if result['missing'] > 0 else "-"
        print(f"{result['name']:<25} {result['mappers']:<10} {result['methods']:<10} {missing_str:<10} {status}")
    
    print("-" * 70)
    print(f"{'總計':<25} {total_mappers:<10} {total_methods:<10} {total_missing:<10}")
    print()
    
    # 最終結果
    print(f"驗證完成: {Color.BOLD}{passed_modules}/{total_modules}{Color.END} 個模組通過")
    print()
    
    if total_missing == 0:
        print(f"{Color.GREEN}{Color.BOLD}✅ 驗證通過！所有 {total_modules} 個模組的 {total_mappers} 個 Mapper 都有完整的 XML 實作{Color.END}")
        print(f"{Color.GREEN}   總共檢查了 {total_methods} 個方法{Color.END}")
        return 0
    else:
        print(f"{Color.RED}{Color.BOLD}❌ 驗證失敗！發現 {total_missing} 個缺失的 XML 實作{Color.END}")
        print(f"{Color.YELLOW}   請修正上述問題後重新驗證{Color.END}")
        return 1

if __name__ == "__main__":
    exit(main())
