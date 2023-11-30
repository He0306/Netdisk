<template>
    <div class="share">
        <div class="header">
            <div class="header-content">
                <div class="logo" @click="jump">
                    <span class="iconfont icon-pan"></span>
                    <span class="name">云盘</span>
                </div>
            </div>
        </div>
    </div>
    <div class="share-body">
        <template v-if="Object.keys(shareInfo).length === 0">
            <div class="loading" v-loading="Object.keys(shareInfo).length === 0"></div>
        </template>
        <template v-else>
            <div class="share-panel">
                <div class="share-user-info">
                    <div class="avatar">
                        <Avatar
                                :userId="shareInfo.userId"
                                :avatar="shareInfo.avatar"
                                :width="50"></Avatar>
                    </div>
                    <div class="share-info">
                        <div class="user-info">
                            <span class="nick-name">{{ shareInfo.nickName }}</span>
                            <span class="share-time">分享于：{{ shareInfo.shareTime }}</span>
                        </div>
                        <div class="file-name">
                            分享文件：{{ shareInfo.fileName }}
                        </div>
                    </div>
                    <div class="share-op-btn">
                        <el-button
                                type="primary"
                                v-if="shareInfo.currentUser"
                                @click="cancelShare">
                            <span class="iconfont icon-cancel"></span>
                            取消分享
                        </el-button>
                        <el-button
                                type="primary"
                                v-else
                                @click="save2MyPan"
                                :disabled="selectFileIdList.length === 0">
                            <span class="iconfont icon-import"></span>
                            保存到我的网盘
                        </el-button>
                    </div>
                </div>
                <Navigation
                        ref="navigationRef"
                        @navChange="navChange"
                        :shareId="shareId">
                </Navigation>
                <div class="file-list">
                    <Table
                            ref="dataTableRef"
                            :columns="columns"
                            :dataSource="tableData"
                            :fetch="loadDataList"
                            :initFetch="false"
                            :options="tableOptions"
                            :showPagination="true"
                            @rowSelected="rowSelected">
                        <template #fileName="{index,row}">
                            <div class="file-item" @mouseenter="showOp(row)" @mouseleave="cancelShowOp(row)">
                                <template v-if=" (row.fileType=== 3 || row.fileType=== 1) && row.status=== 2">
                                    <Icon :cover="row.fileCover" :width="32" @click="preview(row)"></Icon>
                                </template>
                                <template v-else>
                                    <Icon v-if="row.folderType === 0" :fileType="row.fileType"
                                          @click="preview(row)"></Icon>
                                    <Icon v-if="row.folderType === 1" :fileType="0" @click="preview(row)"></Icon>
                                </template>
                                <span :title="row.fileName" class="file-name"></span>
                                <span class="op">
                                    <span v-if="row.folderType === 0" class="iconfont icon-download"
                                          @click="download(row)">下载</span>
                                    <span class="iconfont icon-del" @click="save2MyPanSingle(row)"
                                          v-if="row.showOp && !shareInfo.currentUser">保存到我的网盘</span>
                                </span>
                            </div>
                        </template>
                        <template #fileSize="{index,row}">
                            <span v-if="row.fileSize">{{ proxy.Utils.sizeToStr(row.fileSize) }}</span>
                        </template>
                    </Table>
                </div>
            </div>
        </template>
        <!--    目录选择    -->
        <FolderSelect ref="folderSelectRef" @folderSelect="save2MyPanDone"></FolderSelect>
        <Preview ref="previewRef"></Preview>
    </div>
</template>

<script setup>
import {getCurrentInstance, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import Avatar from "@/components/Avatar.vue";
import Navigation from "@/components/Navigation.vue";
import Icon from "@/components/Icon.vue";
import FolderSelect from "@/components/FolderSelect.vue";
import Preview from "@/components/preview/Preview.vue";
import Table from "@/components/Table.vue";

const {proxy} = getCurrentInstance()
const router = useRouter()
const route = useRoute()
const api = {
    getShareLoginInfo: "/showShare/getShareLoginInfo",
    loadFileList: "/showShare/loadFileList",
    createDownLoadUrl: "/showShare/createDownLoadUrl",
    download: "/api/showShare/download",
    cancelShare: "/share/cancelShare",
    saveShare: "/showShare/saveShare"
}
const shareId = route.params.shareId
const shareInfo = ref({})
const getShareInfo = async () => {
    let result = await proxy.Request({
        url: api.getShareLoginInfo,
        showLoading: false,
        params: {
            shareId
        }
    })
    if (!result) {
        proxy.Message.error(result.message)
        return
    }
    console.log(result)
    if (result.data == null) {
        router.push(`/shareCheck/${shareId}`)
        return
    }
    shareInfo.value = result.data
}
getShareInfo()
// 取消分享
const cancelShare = () => {
    proxy.Confirm(`你确定取消分享吗？`, async () => {
        let result = await proxy.Request({
            url: api.cancelShare,
            params: {
                shareIds: shareId
            }
        })
        if (!result) {
            proxy.Message.error(result.message)
            return
        }
        await loadDataList()
        proxy.Message.success("取消分享成功！")
        router.push("/")
    })
}
const jump = () => {
    router.push("/")
}
// 下载
const download = async (row) => {
    let result = await proxy.Request({
        url: api.createDownLoadUrl + "/" + shareId + "/" + row.fileId
    })
    if (!result) {
        return;
    }
    window.location.href = api.download + "/" + result.data
}

// 保存到我的网盘
const folderSelectRef = ref()
const save2MyPanFileIdArray = []
// 单个保存
const save2MyPanSingle = (row) => {
    if (!proxy.VueCookies.get("userInfo")) {
        router.push("/login?redirectUrl=" + route.path)
        return;
    }
    save2MyPanFileIdArray.value = [row.fileId]
    folderSelectRef.value.showFolderDialog()
}
// 批量保存到我的网盘，并打开文件夹目录
const save2MyPan = () => {
    if (selectFileIdList.value.length === 0) {
        return;
    }
    if (!proxy.VueCookies.get("userInfo")) {
        router.push("/login?redirectUrl=" + route.path)
        return;
    }
    save2MyPanFileIdArray.value = selectFileIdList.value
    folderSelectRef.value.showFolderDialog()
}
// 确定
const save2MyPanDone = async (folderId) => {
    let result = await proxy.Request({
        url: api.saveShare,
        params: {
            shareId: shareId,
            shareFileIds: save2MyPanFileIdArray.value.join(","),
            myFolderId: folderId
        }
    })
    if (!result) {
        proxy.Message.error(result.message)
        return;
    }
    await loadDataList()
    proxy.Message.success("保存成功！")
    folderSelectRef.value.close()
}
// 预览查看
const previewRef = ref()
const navigationRef = ref()
const preview = (data) => {
    if (data.folderType === 1) {
        navigationRef.value.openFolder(data)
        return;
    }
    data.shareId = shareId
    previewRef.value.showPreview(data, 2)
}
const showLoading = ref(true)
// 当前目录
const currentFolder = ref({fileId: 0})
const navChange = (data) => {
    const {curFolder} = data
    currentFolder.value = curFolder
    showLoading.value = true
    loadDataList()
}
const loadDataList = async () => {
    let params = {
        pageNo: tableData.value.pageNo,
        pageSize: tableData.value.pageSize,
        shareId: shareId,
        filePid: currentFolder.value.fileId
    }
    let result = await proxy.Request({
        url: api.loadFileList,
        showLoading: showLoading.value,
        params: params
    })
    if (!result) {
        return;
    }
    tableData.value = result.data
};
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
        selectFileIdList.value.push(item.shareId)
    })
};
const tableData = ref({
    pageNo: 1,
    pageSize: 15
})
const tableOptions = ref({
    extHeight: 50,
    selectType: "checkbox"
})

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
        width: 150
    }
]
</script>

<style lang="scss" scoped>
@import "@/assets/file.list.scss";

.header {
    width: 100%;
    position: fixed;
    background: #0c95f7;
    height: 50px;

    .header-content {
        width: 70%;
        margin: 0 auto;
        color: #ffffff;
        line-height: 50px;

        .logo {
            display: flex;
            align-items: center;
            cursor: pointer;

            .icon-pan {
                font-size: 40px;
            }

            .name {
                font-weight: bold;
                margin-left: 5px;
                font-size: 25px;
            }
        }
    }
}

.share-body {
  width: 70%;
  margin: 0 auto;
  padding-top: 50px;

  .loading {
    height: calc(100vh / 2);
    width: 100%;
  }

  .share-panel {
    margin-top: 20px;
    //display: flex;
    justify-content: space-around;
    border-bottom: 1px solid #dddddd;
    padding-bottom: 10px;

    .share-user-info {
      flex: 1;
      display: flex;
      align-items: center;

      .avatar {
        margin-right: 5px;
      }

      .share-info {
        .user-info {
          display: flex;
          align-items: center;

          .nick-name {
            font-size: 15px;
          }

          .share-time {
            margin-left: 20px;
            font-size: 12px;
          }
        }

        .file-name {
          margin-top: 10px;
          font-size: 12px;
        }
      }
    }
  }
}
.share-op-btn {
    margin-left: 750px;
}

.file-list {
  margin-top: 10px;

  .file-item {
    .op {
      width: 170px;
    }
  }
}
</style>