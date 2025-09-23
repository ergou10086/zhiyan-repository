-- 项目表
CREATE TABLE projects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '项目唯一标识',
    name VARCHAR(200) NOT NULL COMMENT '项目名称',
    description TEXT COMMENT '项目描述',
    status ENUM('PLANNING', 'ONGOING', 'COMPLETED', 'ARCHIVED') DEFAULT 'PLANNING' COMMENT '项目状态（规划中/进行中/已完成/已归档）',
    visibility ENUM('PUBLIC', 'PRIVATE') DEFAULT 'PRIVATE' COMMENT '项目可见性（公开/私有）',
    start_date DATE COMMENT '项目开始日期',
    end_date DATE COMMENT '项目结束日期',
    created_by BIGINT NOT NULL COMMENT '创建人ID（关联users表）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (created_by) REFERENCES users(id) COMMENT '关联创建者用户信息'
) COMMENT '项目基本信息表';


-- 项目成员表
CREATE TABLE project_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成员记录唯一标识',
    project_id BIGINT NOT NULL COMMENT '项目ID（关联projects表）',
    user_id BIGINT NOT NULL COMMENT '用户ID（关联users表）',
    project_role ENUM('LEADER', 'MAINTAINER', 'MEMBER') NOT NULL COMMENT '项目内角色（负责人/维护者/普通成员）',
    permissions_override JSON COMMENT '权限覆盖（JSON格式，用于临时修改成员在项目内的权限）',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入项目时间',
    invited_by BIGINT NOT NULL COMMENT '邀请人ID（关联users表）',
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE COMMENT '项目删除时级联删除成员记录',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE COMMENT '用户删除时级联删除成员记录',
    FOREIGN KEY (invited_by) REFERENCES users(id) COMMENT '关联邀请人信息',
    UNIQUE KEY (project_id, user_id) COMMENT '确保用户不能重复加入同一项目'
) COMMENT '项目成员关系表（记录用户在项目中的角色和权限）';

-- 项目加入申请表
CREATE TABLE project_join_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申请记录唯一标识',
    project_id BIGINT NOT NULL COMMENT '项目ID（关联projects表）',
    user_id BIGINT NOT NULL COMMENT '申请人ID（关联users表）',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' COMMENT '申请状态（待处理/已批准/已拒绝）',
    message TEXT COMMENT '申请说明（申请人填写的加入理由）',
    responded_at TIMESTAMP NULL COMMENT '处理时间',
    responded_by BIGINT NULL COMMENT '处理人ID（关联users表）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE COMMENT '项目删除时级联删除申请记录',
    FOREIGN KEY (user_id) REFERENCES users(id) COMMENT '关联申请人信息',
    FOREIGN KEY (responded_by) REFERENCES users(id) COMMENT '关联处理人信息',
    UNIQUE KEY (project_id, user_id, status) COMMENT '同一用户对同一项目的同一状态申请唯一'
) COMMENT '用户加入项目的申请表';