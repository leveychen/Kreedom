package com.goxod.freedom.config.type

import com.goxod.freedom.service.ApiAbstract
import com.goxod.freedom.service.impl.*

enum class ApiItem(
    val title: String,
    val apiId: Int,
    val api: ApiAbstract,
    val key: String
) {
    API_40001("长篇", 40001, Api40001(), "E46xhNaOm6U5UDfKr7NXbFCZOyK9lTKZ5ePDOf/mT74="),
//    API_20001("自拍", 20001, Api20001(), "YsPleuGIuUHa1LEuP4oPsXdQoAOBZFi30gbs5tPBJ88="),
    API_20001("自拍", 20001, Api20001(), "4dyAYMOzJfg4J6C3hcRZ3SP2yNWZvf+DAIhI1DENIe0="),
    API_10001("短片", 10001, Api10001(), "meiUs1wUjyiaXSFp9RMy20YxQ2MZVekQU1iyf8i+/pY="),
    FAVORITE("我的", 99999, ApiFavorite(), "favorite");
}