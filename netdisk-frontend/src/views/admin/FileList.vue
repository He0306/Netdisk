<template>
    <div>
        <div class="top">
            <div class="top-op">
                <div class="search-panel">
                    <el-input v-model="fileNameFuzzy" clearable placeholder="请输入文件名/昵称搜索" @keyup.enter="search">
                        <template #suffix>
                            <i class="iconfont icon-search" @click="search"></i>
                        </template>
                    </el-input>
                </div>
                <div class="iconfont icon-refresh" @click="loadDataList"></div>
                <el-button :style="{ 'margin-left': '10px' }" :disabled="selectFileIdList.length === 0" type="danger" @click="delFileBatch">
                    <span class="iconfont icon-del"></span>
                    批量删除
                </el-button>
            </div>
            <!--  导航    -->
            <div>
                <Navigation ref="navigationRef" @navChange="navChange"></Navigation>
            </div>
        </div>

        <div v-if="tableData.list && tableData.list.length>0" class="file-list">
            <Table
                    ref="dataTableRef"
                    :columns="columns"
                    :dataSource="tableData"
                    :fetch="loadDataList"
                    :initFetch="false"
                    :options="tableOptions"
                    :showPagination="true"
                    @rowSelected="rowSelected"
            >
                <template #fileName="{index,row}">
                    <div class="file-item" @mouseenter="showOp(row)" @mouseleave="cancelShowOp(row)">
                        <template v-if=" (row.fileType=== 3 || row.fileType=== 1) && row.status=== 2">
                            <Icon :cover="row.fileCover" :width="32" @click="preview(row)"></Icon>
                        </template>
                        <template v-else>
                            <Icon v-if="row.folderType === 0" :fileType="row.fileType" @click="preview(row)"></Icon>
                            <Icon v-if="row.folderType === 1" :fileType="0" @click="preview(row)"></Icon>
                        </template>
                        <span v-if="!row.showEdit" :title="row.fileName" class="file-name">
                          <span @click="preview(row)">{{ row.fileName }}</span>
                          <span v-if="row.status === 0" class="transfer-status">转码中</span>
                          <span v-if="row.status === 1" class="transfer-status transfer-fail">转码失败</span>
                        </span>
                        <div v-if="row.showEdit" class="edit-panel">
                            <el-input
                                    v-model.trim="row.fileNameReal"
                                    ref="editNameRef"
                                    :maxLength="190"
                                    @keyup.enter="saveNameEdit(index)"
                            >
                                <template #suffix>{{ row.fileSuffix }}</template>
                            </el-input>
                            <span :class="['iconfont icon-right1',row.fileNameReal ? '' : 'not-allow']"
                                  @click="saveNameEdit(index)">
                            </span>
                            <span class="iconfont icon-error" @click="cancelNameEdit(index)"></span>
                        </div>
                        <span class="op">
                          <template v-if="row.showOp && row.fileId && row.status===2">
                            <span v-if="row.folderType === 0" class="iconfont icon-download"
                                  @click="download(row)">下载</span>
                            <span class="iconfont icon-del" @click="delFile(row)">删除</span>
                          </template>
                        </span>
                    </div>
                </template>

                <template #fileSize="{index,row}">
                    <span v-if="row.fileSize">{{ proxy.Utils.sizeToStr(row.fileSize) }}</span>
                </template>
            </Table>
        </div>

        <!-- 预览组件 -->
        <Preview ref="previewRef"></Preview>
    </div>
</template>

<script setup>

import Table from "@/components/Table.vue";
import {useRoute, useRouter} from "vue-router";
import {getCurrentInstance, ref} from "vue";
import Icon from "@/components/Icon.vue";
import Navigation from "@/components/Navigation.vue";
import Preview from "@/components/preview/Preview.vue";

const router = useRouter()
const route = useRoute()
const {proxy} = getCurrentInstance()

const api = {
    loadDataList: "/admin/loadFileList",
    delFile: "/admin/delFile",
    createDownLoadUrl: "/admin/createDownLoadUrl",
    download: "/api/admin/download"
}
const tableData = ref({
    pageNo: 1,
    pageSize: 15
})
const tableOptions = ref({
    extHeight: 50,
    selectType: "checkbox"
})
const fileNameFuzzy = ref();
const showLoading = ref(true)
const loadDataList = async () => {
    let params = {
        pageNo: tableData.value.pageNo,
        pageSize: tableData.value.pageSize,
        fileNameFuzzy: fileNameFuzzy.value
    }
    let result = await proxy.Request({
        url: api.loadDataList,
        showLoading: showLoading.value,
        params: params
    })
    if (!result) {
        return;
    }
    tableData.value = result.data
};
// 当前目录
const currentFolder = ref({fileId: 0})
const navChange = (data) => {
    const { curFolder} = data
    currentFolder.value = curFolder
    showLoading.value = true
    loadDataList()
}

// 预览
const navigationRef = ref()
const previewRef = ref()
const preview = (data) => {
    // 目录
    if (data.folderType === 1) {
        navigationRef.value.openFolder(data)
        return;
    }
    // 文件
    if (data.status !== 2) {
        proxy.Message.warning("文件未完成转码，无法预览")
        return;
    }
    previewRef.value.showPreview(data, 1)
}

// 展示操作按钮
const showOp = (row) => {
    tableData.value.list.forEach((element) => {
        element.showOp = false;
    })
    row.showOp = true;
}
const cancelShowOp = (row) => {
    row.showOp = true;
}

// 多选
const selectFileIdList = ref([])
const rowSelected = (rows) => {
    selectFileIdList.value = []
    rows.forEach((item) => {
        selectFileIdList.value.push(item.userId + "_" + item.fileId)
    })
};
// 下载
const download = async (row) => {
    let result = await proxy.Request({
        url: api.createDownLoadUrl + "/" + row.userId + "/" + row.fileId
    })
    if (!result) {
        return;
    }
    window.location.href = api.download + "/" + result.data
}
// 批量删除
const delFileBatch = () => {
    if (selectFileIdList.value.length === 0) {
        return;
    }
    proxy.Confirm(`你确定要删除这些文件吗？删除的文件可在10天内通过回收站还原`,
        async () => {
            let result = await proxy.Request({
                url: api.delFile,
                params: {
                    fileIdAndUserIds: selectFileIdList.value.join(",")
                }
            })
            if (!result) {
                return;
            }
            await loadDataList()
        }
    )
}
// 单删除
const delFile = (row) => {
    proxy.Confirm(`你确定删除【${row.fileName}】吗？删除的文件可在10天内通过回收站还原`,
        async () => {
            let result = await proxy.Request({
                url: api.delFile,
                params: {
                    fileIdAndUserIds: row.userId + "_" + row.fileId
                }
            })
            if (!result) {
                return;
            }
            await loadDataList()
        }
    )
}

// 搜索
const search = () => {
    showLoading.value = true
    loadDataList()
}

const columns = [
    {
        label: "文件名",
        prop: "fileName",
        scopedSlots: "fileName"
    },
    {
        label: "发布人",
        prop: "nickName",
        width: 200
    },
    {
        label: "修改时间",
        prop: "lastUpdateTime",
        width: 200
    },
    {
        label: "大小",
        prop: "fileSize",
        scopedSlots: "fileSize",
        width: 200
    }
]
</script>

<style lang="scss" scoped>
@import "@/assets/file.list.scss";

//.search-panel {
//    margin-left: 0 !important;
//}
//.file-list {
//  margin-top: 10px;
//
//  .file-item {
//    .op {
//      width: 120px;
//    }
//  }
//}
</style>
