package com.hc.common.enums;

/**
 * @author: 何超
 * @date: 2023-06-09 21:40
 * @description:
 */
public enum HttpCodeEnum {

    CODE_200(200, "请求成功！"),
    CODE_400(400, "请求失败！"),
    CODE_404(404, "请求地址不存在！"),
    CODE_405(405, "图片验证码不正确！"),
    CODE_406(406, "邮箱账号不存在！"),
    CODE_407(407, "邮箱账号已存在！"),
    CODE_408(408, "昵称已存在！"),
    CODE_409(409, "账号或密码错误！"),
    CODE_410(410, "账号已被禁用，请联系管理员！"),
    CODE_411(411, "邮箱验证码不正确！"),
    CODE_412(412, "邮箱验证码已失效！"),
    CODE_413(413, "获取qqToken失败！"),
    CODE_414(414, "调qq接口获取openID失败！"),
    CODE_415(415, "调qq接口获取用户信息失败！"),
    CODE_416(416, "目录不存在！"),
    CODE_417(417, "合并分片失败！"),
    CODE_418(418, "合并文件失败！"),
    CODE_419(419, "视频转换失败！"),
    CODE_420(420, "此目录下已经存在同名文件，请修改名称！"),
    CODE_421(421, "文件不存在！"),
    CODE_422(422, "文件名已存在！"),
    CODE_423(423, "当前文件在此目录下，不能进行移动！"),
    CODE_424(424, "此目录已经删除，不能进行移动！"),
    CODE_425(425, "下载文件不存在！"),
    CODE_426(426, "该文件已在回收站中！"),
    CODE_427(427, "不能操作当前登录用户！"),
    CODE_428(428, "所传值不能为空！"),
    CODE_429(429, "所传值长度错误！"),
    CODE_430(430, "正则校验错误！"),
    CODE_431(431, "文件正在分享中不能删除！"),
    CODE_432(432, "分享链接不存在，或者已失效！"),
    CODE_433(433, "提取码错误，请重新输入！"),
    CODE_434(434, "分享验证失败，请重新验证！"),
    CODE_435(435, "自己分享的文件无法保存到自己的网盘！"),
    CODE_500(500, "服务器内部错误，请联系管理员！"),
    CODE_600(600, "请求参数错误！"),
    CODE_901(901, "登录超时，请重新登录！"),
    CODE_902(902, "上传文件大小超过限制！"),
    CODE_904(904, "网盘空间不足，请重新扩容");

    private Integer status;

    private String message;

    HttpCodeEnum(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

    public Integer getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
