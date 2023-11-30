<template>
    <div>
        <div class="top">
            <div class="top-op">
                <div class="btn">
                    <el-upload
                            :accept="fileAccept"
                            :http-request="addFile"
                            :multiple="true"
                            :show-file-list="false"
                            :with-credentials="true"
                    >
                        <el-button type="primary">
                            <span class="iconfont icon-upload"></span>
                            上传
                        </el-button>
                    </el-upload>
                </div>
                <el-button v-if="category === 'all'" type="success" @click="newFolder">
                    <span class="iconfont icon-folder-add"></span>
                    新建文件夹
                </el-button>
                <el-button :disabled="selectFileIdList.length === 0" type="danger" @click="delFileBatch">
                    <span class="iconfont icon-del"></span>
                    批量删除
                </el-button>
                <el-button :disabled="selectFileIdList.length === 0" type="warning" @click="moveFolderBatch">
                    <span class="iconfont icon-move"></span>
                    批量移动
                </el-button>
                <div class="search-panel">
                    <el-input v-model="fileNameFuzzy" clearable placeholder="请输入文件名搜索" @keyup.enter="search">
                        <template #suffix>
                            <i class="iconfont icon-search" @click="search"></i>
                        </template>
                    </el-input>
                </div>
                <div class="iconfont icon-refresh" @click="loadDataList"></div>
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
                            <span class="iconfont icon-share" @click="share(row)">分享</span>
                            <span v-if="row.folderType === 0" class="iconfont icon-download"
                                  @click="download(row)">下载</span>
                            <span class="iconfont icon-del" @click="delFile(row)">删除</span>
                            <span class="iconfont icon-edit" @click="editFileName(index)">重命名</span>
                            <span class="iconfont icon-move" @click="moveFolder(row)">移动</span>
                          </template>
                        </span>
                    </div>
                </template>

                <template #fileSize="{index,row}">
                    <span v-if="row.fileSize">{{ proxy.Utils.sizeToStr(row.fileSize) }}</span>
                </template>
            </Table>
        </div>
        <div v-else class="no-data">
            <div class="no-data-inner">
                <Icon :width="120" fit="fill" iconName="no_data"></Icon>
                <div class="tips">当前目录为空，上传你的第一个文件吧</div>
                <div class="op-list">
                    <el-upload
                            :accept="fileAccept"
                            :http-request="addFile"
                            :multiple="true"
                            :show-file-list="false"
                            :with-credentials="true"
                    >
                        <div class="op-item">
                            <Icon :width="60" iconName="file"></Icon>
                            <div>上传文件</div>
                        </div>
                    </el-upload>
                    <div v-if="category === 'all'" class="op-item" @click="newFolder">
                        <Icon :width="60" iconName="folder"></Icon>
                        <div>新建目录</div>
                    </div>
                </div>
            </div>
        </div>
        <!-- 移动组件 -->
        <FolderSelect ref="folderSelectRef" @folderSelect="moveFolderDone"></FolderSelect>
        <!-- 预览组件 -->
        <Preview ref="previewRef"></Preview>
        <!-- 分享 -->
        <ShareFile ref="shareRef"></ShareFile>
    </div>
</template>

<script setup>

import Table from "@/components/Table.vue";
import {useRoute, useRouter} from "vue-router";
import {computed, getCurrentInstance, nextTick, ref} from "vue";
import Icon from "@/components/Icon.vue";
import CategoryInfo from "@/js/CategoryInfo";
import FolderSelect from "@/components/FolderSelect.vue";
import Navigation from "@/components/Navigation.vue";
import Preview from "@/components/preview/Preview.vue";
import ShareFile from "@/components/ShareFile.vue"

const router = useRouter()
const route = useRoute()
const {proxy} = getCurrentInstance()

const api = {
    loadDataList: "/file/loadDataList",
    rename: "/file/rename",
    newFolder: "/file/newFolder",
    getFolderInfo: "/file/getFolderInfo",
    delFile: "/file/delFile",
    changeFileFolder: "/file/changeFileFolder",
    createDownLoadUrl: "/file/createDownLoadUrl",
    download: "/api/file/download"
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
const category = ref();
const loadDataList = async () => {
    let params = {
        pageNo: tableData.value.pageNo,
        pageSize: tableData.value.pageSize,
        fileNameFuzzy: fileNameFuzzy.value,
        filePid: currentFolder.value.fileId,
        category: category.value
    }
    if (params.category !== "all") {
        delete params.filePid
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

// 多选
const selectFileIdList = ref([])
const rowSelected = (rows) => {
    selectFileIdList.value = []
    rows.forEach((item) => {
        selectFileIdList.value.push(item.fileId)
    })
};
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
                    fileIds: selectFileIdList.value.join(",")
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
// 单删除
const delFile = (row) => {
    proxy.Confirm(`你确定删除【${row.fileName}】吗？删除的文件可在10天内通过回收站还原`,
        async () => {
            let result = await proxy.Request({
                url: api.delFile,
                params: {
                    fileIds: row.fileId
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

// 批量移动
const folderSelectRef = ref()
const currentMoveFile = ref({})
const moveFolderBatch = () => {
    folderSelectRef.value.showFolderDialog(currentFolder.value.fileId)
}

const moveFolder = (data) => {
    currentMoveFile.value = data
    folderSelectRef.value.showFolderDialog(currentFolder.value.fileId)
}

// 提交移动
const moveFolderDone = async (folderId) => {
    if (currentFolder.value.fileId === folderId) {
        proxy.Message.warning("文件正在当前目录，无需移动")
        return;
    }
    let fileIdsArray = [];
    if (currentMoveFile.value.fileId) {
        fileIdsArray.push(currentMoveFile.value.fileId)
    } else {
        fileIdsArray = fileIdsArray.concat(selectFileIdList.value)
    }
    let result = await proxy.Request({
        url: api.changeFileFolder,
        params: {
            fileIds: fileIdsArray.join(","),
            filePid: folderId
        }
    })
    if (!result) {
        return;
    }
    folderSelectRef.value.close()
    proxy.Message.success("移动成功！")
    await loadDataList()
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

// 编辑行
const editing = ref(false);
const editNameRef = ref();
// 新建文件夹
const newFolder = () => {
    if (editing.value) {
        return;
    }
    tableData.value.list.forEach((element) => {
        element.showEdit = false;
    });
    editing.value = true
    tableData.value.list.unshift({
        showEdit: true,
        fileType: 0,
        fileId: "",
        filePid: 0
    });
    nextTick(() => {
        editNameRef.value.focus()
    });
}

// 取消新建目录
const cancelNameEdit = (index) => {
    const fileData = tableData.value.list[index];
    if (fileData.fileId) {
        fileData.showEdit = false
    } else {
        tableData.value.list.splice(index, 1)
    }
    editing.value = false
}

// 确定新建目录
const saveNameEdit = async (index) => {
    const {fileId, filePid, fileNameReal} = tableData.value.list[index];
    if (fileNameReal === "" || fileNameReal.indexOf("/") !== -1) {
        proxy.Message.warning("文件名不能为空且不能含有斜杠")
        return;
    }
    let url = api.rename;
    if (fileId === "") {
        url = api.newFolder
    }
    let result = await proxy.Request({
        url: url,
        params: {
            fileId: fileId,
            filePid: filePid,
            fileName: fileNameReal
        }
    })
    if (!result) {
        return;
    }
    tableData.value.list[index] = result.data
    proxy.Message.success("操作成功！")
    proxy.Message.success("操作成功！")
    editing.value = false
}

// 重命名
const editFileName = (index) => {
    if (tableData.value.list[0].fileId === "") {
        tableData.value.list.splice(0, 1)
        index = index - 1
    }
    tableData.value.list.forEach((element) => {
        element.showEdit = false
    })
    let currentData = tableData.value.list[index]
    currentData.showEdit = true
    // 编辑文件
    if (currentData.folderType === 0) {
        currentData.fileNameReal = currentData.fileName.substring(0, currentData.fileName.indexOf("."))
        currentData.fileSuffix = currentData.fileName.substring(currentData.fileName.indexOf("."))
    } else {
        currentData.fileNameReal = currentData.fileName
        currentData.fileSuffix = ""
    }
    editing.value = true
    nextTick(() => {
        editNameRef.value.focus()
    })
}

// 上传
const emit = defineEmits(["addFile"])
// 当前目录
const currentFolder = ref({fileId: 0})
// 传入上传组件
const addFile = (fileData) => {
    emit("addFile", {file: fileData.file, filePid: currentFolder.value.fileId})
}

const fileAccept = computed(() => {
    const categoryItem = CategoryInfo[category.value]
    return categoryItem ? categoryItem.accept : "*"
})

// 搜索
const search = () => {
    showLoading.value = true
    loadDataList()
}
// 添加文件回调
const reload = () => {
    showLoading.value = false
    loadDataList()
}
defineExpose({
    reload
})

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
    previewRef.value.showPreview(data, 0)
}

const navChange = (data) => {
    const {categoryId, curFolder} = data
    currentFolder.value = curFolder
    category.value = categoryId
    loadDataList()
}

// 下载
const download = async (row) => {
    let result = await proxy.Request({
        url: api.createDownLoadUrl + "/" + row.fileId
    })
    if (!result) {
        return;
    }
    window.location.href = api.download + "/" + result.data
}
// 分享
const shareRef = ref()
const share = (row) => {
    shareRef.value.show(row)
}

const columns = [
    {
        label: "文件名",
        prop: "fileName",
        scopedSlots: "fileName"
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
</style>
