<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    // 获取数据库连接状态（暂时写死，后续可从应用上下文获取）
    String dbStatus = "已连接";
    String dbStatusClass = "text-success";
    
    // 可以后续从应用上下文获取真实状态
    // Object dbStatusObj = application.getAttribute("dbStatus");
    // if (dbStatusObj != null) {
    //     dbStatus = dbStatusObj.toString();
    //     dbStatusClass = dbStatus.equals("已连接") ? "text-success" : "text-danger";
    // }
%>
<!-- 结束主内容与壳结构；页脚信息已移动到左侧侧边栏 -->
        </div>
    </div>
</div>

