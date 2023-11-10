<template>
    <div>
        <div class="top">
            <div class="top-panel">
                <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="100px" @submit.prevent>
                    <el-row>
                        <el-col :span="6">
                            <el-form-item label="用户昵称">
                                <el-input clearable placeholder="请输入用户昵称"
                                          v-model.trim="formData.nickName"
                                          @keyup.native="loadDataList"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="6">
                            <el-form-item label="状态">
                                <el-select
                                        clearable
                                        placeholder="请选择状态"
                                        v-model="formData.status">
                                    <el-option :value="1" label="启用"></el-option>
                                    <el-option :value="0" label="禁用"></el-option>
                                </el-select>
                            </el-form-item>
                        </el-col>
                        <el-col :span="4" style="margin-left: 10px">
                            <el-button type="primary" @click="loadDataList" :icon="Search">搜索</el-button>
                            <el-button type="warning" @click="rest" :icon="Refresh">重置</el-button>
                        </el-col>
                    </el-row>
                </el-form>
                <div class="file-list">
                    <Table
                            ref="dataTableRef"
                            :columns="columns"
                            :dataSource="tableData"
                            :fetch="loadDataList"
                            :initFetch="true"
                            :options="tableOptions"
                            :showPagination="true"
                    >
                        <template #avatar="{ index,row }">
                            <div>
                                <Avatar :userId="row.userId" :avatar="row.qqAvatar"></Avatar>
                            </div>
                        </template>
                        <template #space="{ index,row }">
                            {{ proxy.Utils.sizeToStr(row.userSpace) }} / {{ proxy.Utils.sizeToStr(row.totalSpace) }}
                        </template>
                        <template #status="{ index,row }">
                            <span v-if="row.status === 1" style="color: #529b2e">启用</span>
                            <span v-if="row.status === 0" style="color: #f56c62">禁用</span>
                        </template>
                        <template #op="{ index,row }">
                            <span class="a-link" @click="updateSpace(row)">分配空间</span>
                            <el-divider direction="vertical"></el-divider>
                            <span class="a-link" @click="updateUserSpace(row)">
                            {{ row.status === 0 ? "启用" : "禁用" }}
                        </span>
                            <el-divider direction="vertical"></el-divider>
                            <el-link type="danger" :underline="false" @click="deleteUser(row)">删除</el-link>
                        </template>
                    </Table>
                </div>

                <div>
                    <!--  弹窗  -->
                    <el-dialog v-model="dialogFormVisible" title="修改头像" width="450" center>
                        <el-form label-width="100px" label-position="right" :model="form" ref="formRef"
                                 :rules="formRules">
                            <el-form-item label="昵称">
                                {{ form.nickName }}
                            </el-form-item>
                            <el-form-item label="空间大小" prop="changeSpace">
                                <el-input
                                        clearable
                                        placeholder="请输入空间大小"
                                        v-model.number="form.changeSpace">
                                    <template #suffix>MB</template>
                                </el-input>
                            </el-form-item>
                        </el-form>
                        <template #footer>
                        <span class="dialog-footer">
                             <el-button type="primary" @click="submitForm">确定</el-button>
                        </span>
                        </template>
                    </el-dialog>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import {Search, Refresh} from '@element-plus/icons-vue'
import {getCurrentInstance, nextTick, ref} from "vue";
import Table from "@/components/Table.vue";
import Avatar from "@/components/Avatar.vue";

const {proxy} = getCurrentInstance()
const formData = ref({})
const rules = ref()
const formDataRef = ref()
const form = ref({})
const formRules = {
    changeSpace: [{required: true, message: "请输入空间大小", trigger: 'blur'}]
}
const formRef = ref()

const api = {
    loadDataList: "/admin/loadUserList",
    updateUserStatus: "/admin/updateUserStatus",
    updateUserSpace: "/admin/updateUserSpace",
    deleteUser: "/admin/deleteUser"
}
const dialogFormVisible = ref(false)
const tableData = ref({
    pageNo: 1,
    pageSize: 15
})
const tableOptions = ref({
    extHeight: 20,
})
const deleteUser = (row) => {
    proxy.Confirm(`你确定删除该【${row.nickName}】用户吗？`,
        async () => {
            let result = await proxy.Request({
                url: api.deleteUser,
                params: {
                    userId: row.userId
                }
            })
            if (!result) {
                return;
            }
            proxy.Message.success("删除成功！")
            await loadDataList()
        }
    )
}
const updateSpace = (row) => {
    dialogFormVisible.value = true
    nextTick(() => {
        formRef.value.resetFields();
        form.value = Object.assign({}, row)
    })
}
const submitForm = () => {
    formRef.value.validate(async (valid) => {
        if (!valid) {
            return
        }
        let params = {}
        Object.assign(params, form.value)
        let result = await proxy.Request({
            url: api.updateUserSpace,
            params
        })
        if (!result) {
            return
        }
        dialogFormVisible.value = false
        proxy.Message.success("分配空间成功！")
        loadDataList()
    })
}
const updateUserSpace = (row) => {
    proxy.Confirm(`你确定${row.status === 0 ? "启用" : "禁用"}该【${row.nickName}】用户吗？`,
        async () => {
            let result = await proxy.Request({
                url: api.updateUserStatus,
                params: {
                    userId: row.userId,
                    status: row.status === 0 ? 1 : 0
                }
            })
            if (!result) {
                return;
            }
            proxy.Message.success("操作成功！")
            await loadDataList()
        }
    )
}
// 加载全部信息
const loadDataList = async () => {
    let params = {
        pageNo: tableData.value.pageNo,
        pageSize: tableData.value.pageSize
    }
    Object.assign(params, formData.value)
    let result = await proxy.Request({
        url: api.loadDataList,
        params
    })
    if (!result) {
        return
    }
    tableData.value = result.data
}
// 重置
const rest = () => {
    formData.value = {}
    loadDataList()
}

const columns = [
    {
        label: "头像",
        prop: "avatar",
        width: 80,
        scopedSlots: "avatar"
    },
    {
        label: "昵称",
        prop: "nickName"
    },
    {
        label: "邮箱",
        prop: "email"
    },
    {
        label: "空间使用",
        prop: "space",
        scopedSlots: "space"
    },
    {
        label: "加入时间",
        prop: "joinTime"
    },
    {
        label: "最后登录时间",
        prop: "lastLoginTime"
    },
    {
        label: "状态",
        prop: "status",
        scopedSlots: "status",
        width: 80
    },
    {
        label: "操作",
        prop: "op",
        width: 200,
        scopedSlots: "op"
    }
]
</script>

<style lang="scss" scoped>
.top-panel {
  margin-top: 10px;
}
.top{
    margin-top: 20px;
}
</style>
