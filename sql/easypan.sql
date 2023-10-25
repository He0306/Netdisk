/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80012
 Source Host           : localhost:3306
 Source Schema         : easypan

 Target Server Type    : MySQL
 Target Server Version : 80012
 File Encoding         : 65001

 Date: 25/10/2023 22:59:23
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_email_code
-- ----------------------------
DROP TABLE IF EXISTS `tb_email_code`;
CREATE TABLE `tb_email_code`  (
  `email` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '邮箱',
  `code` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '编号',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '状态（0：禁用 1：启用）',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`email`, `code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '邮箱验证码' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_email_code
-- ----------------------------
INSERT INTO `tb_email_code` VALUES ('2740860037@qq.com', '159603', 1, '2023-06-07 23:10:34');
INSERT INTO `tb_email_code` VALUES ('2740860037@qq.com', '197279', 1, '2023-06-09 16:38:13');
INSERT INTO `tb_email_code` VALUES ('2740860037@qq.com', '269178', 1, '2023-06-08 15:11:42');
INSERT INTO `tb_email_code` VALUES ('2740860037@qq.com', '525632', 1, '2023-06-10 17:18:24');
INSERT INTO `tb_email_code` VALUES ('2740860037@qq.com', '546000', 1, '2023-06-10 16:48:04');
INSERT INTO `tb_email_code` VALUES ('2740860037@qq.com', '709639', 1, '2023-06-09 17:06:03');
INSERT INTO `tb_email_code` VALUES ('2740860037@qq.com', '811561', 1, '2023-06-10 17:14:36');
INSERT INTO `tb_email_code` VALUES ('2740860037@qq.com', '985634', 1, '2023-06-10 17:27:12');
INSERT INTO `tb_email_code` VALUES ('2740860037@qq.com', '993442', 1, '2023-06-09 17:16:49');

-- ----------------------------
-- Table structure for tb_file_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_file_info`;
CREATE TABLE `tb_file_info`  (
  `file_id` char(19) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件ID',
  `user_id` char(19) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
  `file_md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'MD5',
  `file_pid` char(19) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '父ID',
  `file_size` bigint(20) NULL DEFAULT NULL COMMENT '长度',
  `file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件名',
  `file_cover` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面',
  `file_path` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件路径',
  `folder_type` tinyint(1) NULL DEFAULT NULL COMMENT '0：文件  1：目录',
  `file_category` tinyint(1) NULL DEFAULT NULL COMMENT '1：视频 2：音频 3：图片 4：文档 5：其它',
  `file_type` tinyint(1) NULL DEFAULT NULL COMMENT '1：视频 2：音频 3：图片 4：pdf 5：doc 6：excel 7：txt 8：code 9：zip 10：其它',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '0：转码中 1：转码失败 2：转码成功',
  `recovery_time` datetime NULL DEFAULT NULL COMMENT '进入回收站时间',
  `del_flag` tinyint(1) NULL DEFAULT NULL COMMENT '0：删除 1：回收站 2：正常',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `last_update_time` datetime NULL DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`file_id`, `user_id`) USING BTREE,
  INDEX `key_create_time`(`create_time` ASC) USING BTREE,
  INDEX `key_user_id`(`user_id` ASC) USING BTREE,
  INDEX `key_file_md5`(`file_md5` ASC) USING BTREE,
  INDEX `key_file_pid`(`file_pid` ASC) USING BTREE,
  INDEX `key_del_flag`(`del_flag` ASC) USING BTREE,
  INDEX `key_recovery_time`(`recovery_time` ASC) USING BTREE,
  INDEX `key_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文件信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_file_info
-- ----------------------------
INSERT INTO `tb_file_info` VALUES ('mdhw21XvS6', '1667464398215524354', 'f1f902c3dfe72cfa0838d2864b37838c', '0', 16986, '库存查询.xlsx', NULL, '202310/1667464398215524354mdhw21XvS6.xlsx', 0, 4, 6, 2, NULL, 2, '2023-10-22 11:30:52', '2023-10-22 11:30:52');
INSERT INTO `tb_file_info` VALUES ('tB4Na5kVxB', '1667464398215524354', '8dcdd8a7e819ca7b721452be92f31881', '0', 272298, '2.png', '202310/1667464398215524354tB4Na5kVxB_.png', '202310/1667464398215524354tB4Na5kVxB.png', 0, 3, 3, 2, NULL, 2, '2023-10-22 23:37:22', '2023-10-22 23:37:22');
INSERT INTO `tb_file_info` VALUES ('V1xhhgKNiT', '1667464398215524354', 'f0656e56c8ad637a7e51be459ea99cdb', '0', 96748, '1.jpg', '202310/1667464398215524354V1xhhgKNiT_.jpg', '202310/1667464398215524354V1xhhgKNiT.jpg', 0, 3, 3, 2, NULL, 2, '2023-10-22 23:22:25', '2023-10-22 23:22:25');

-- ----------------------------
-- Table structure for tb_share
-- ----------------------------
DROP TABLE IF EXISTS `tb_share`;
CREATE TABLE `tb_share`  (
  `share_id` char(19) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
  `file_id` char(19) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件ID',
  `user_id` char(19) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分享ID',
  `valid_type` tinyint(1) NULL DEFAULT NULL COMMENT '有效期  0:1天  1:7天  2:30天  3:永久有效',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `share_time` datetime NULL DEFAULT NULL COMMENT '分享时间',
  `code` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提取码',
  `show_count` int(11) NULL DEFAULT 0 COMMENT '查看次数',
  PRIMARY KEY (`share_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_share
-- ----------------------------

-- ----------------------------
-- Table structure for tb_user_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_info`;
CREATE TABLE `tb_user_info`  (
  `user_id` char(19) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
  `nick_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `qq_open_id` varchar(35) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'qqID',
  `qq_avatar` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'qq头像',
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '密码\r\n',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '状态（0：禁用  1：启用）',
  `user_space` bigint(20) NULL DEFAULT NULL COMMENT '使用空间单位 byte',
  `total_space` bigint(20) NULL DEFAULT NULL COMMENT '总空间',
  `join_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `key_email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `key_qq_open_id`(`qq_open_id` ASC) USING BTREE,
  UNIQUE INDEX `key_nick_name`(`nick_name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_user_info
-- ----------------------------
INSERT INTO `tb_user_info` VALUES ('1667464398215524354', '何超', '2740860037@qq.com', NULL, '', '464fa6d27101895f8e9b5ecf70a2f4d1', 1, 386032, 3276800000, '2023-06-10 17:31:07', '2023-10-25 21:52:58');

SET FOREIGN_KEY_CHECKS = 1;
