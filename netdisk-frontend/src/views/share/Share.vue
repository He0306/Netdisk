<template>
    <div>
        <div class="top">
            <el-button type="primary" :disabled="selectIdList.length === 0" @click="cancelShareBatch">
                <span class="iconfont icon-cancel">取消分享</span>
            </el-button>
        </div>
        <div class="file-list">
            <Table
                    ref="dataTableRef"
                    :columns="columns"
                    :dataSource="tableData"
                    :fetch="loadDataList"
                    :initFetch="true"
                    :options="tableOptions"
                    :showPagination="true"
                    @rowSelected="rowSelected">
                <template #fileName="{ index, row }">
                    <div
                            class="file-item"
                            @mouseenter="showOp(row)"
                            @mouseleave="cancelShowOp(row)"
                    >
                        <template v-if="(row.fileType === 3 || row.fileType === 1) && row.status !== 0">
                            <Icon :cover="row.fileCover"></Icon>
                        </template>
                        <template v-else>
                            <Icon v-if="row.folderType === 0" :fileType="row.fileType"></Icon>
                            <Icon v-if="row.folderType === 1" :fileType="0"></Icon>
                        </template>
                        <span class="file-name" :title="row.fileName">
                            {{ row.fileName }}
                        </span>
                        <span class="op">
                            <template v-if="row.showOp">
                                <span class="iconfont icon-link" @click="copy(row)">
                                    复制链接
                                </span>
                                <span class="iconfont icon-cancel" @click="cancelShare(row)">
                                    取消分享
                                </span>
                            </template>
                        </span>
                    </div>
                </template>
                <template #expireTime="{ index, row }">
                    {{ row.validType === 3 ? "永久" : row.expireTime }}
                </template>
            </Table>
        </div>
    </div>
</template>

<script setup>
import useClipboard from "vue-clipboard3"
import Table from "@/components/Table.vue";
import Icon from "@/components/Icon.vue";
import {getCurrentInstance, ref} from "vue";

const {toClipboard} = useClipboard()

const {proxy} = getCurrentInstance()
const api = {
    loadDataList: "/share/loadShareList",
    cancelShare: "/share/cancelShare"
}
const tableData = ref({
    pageNo: 1,
    pageSize: 15
})
const tableOptions = ref({
    extHeight: 50,
    selectType: "checkbox"
})
const loadDataList = async () => {
    let params = {
        pageNo: tableData.value.pageNo,
        pageSize: tableData.value.pageSize
    }
    let result = await proxy.Request({
        url: api.loadDataList,
        params: params
    })
    if (!result) {
        return;
    }
    tableData.value = result.data
};
// 多选
const selectIdList = ref([])
const rowSelected = (rows) => {
    selectIdList.value = []
    rows.forEach((item) => {
        selectIdList.value.push(item.shareId)
    })
};
const showOp = (row) => {
    tableData.value.list.forEach((item) => {
        item.showOp = false
    })
    row.showOp = true
}
const cancelShowOp = (row) => {
    row.showOp = false
}
// 复制链接
const shareUrl = ref(document.location.origin + "/share/")
const copy = async (data) => {
    await toClipboard(`链接：${shareUrl.value}${data.shareId} 提取码，${data.code}`)
    proxy.Message.success("复制成功！")
}
// 取消分享
const cancelShareIdList = ref([])
const cancelShare = (row) => {
    cancelShareIdList.value = [row.shareId]
    cancelShareDone()
}
// 批量取消分享
const cancelShareBatch = () => {
    if (selectIdList.value.length === 0) {
        return;
    }
    cancelShareIdList.value = selectIdList.value
    cancelShareDone();
}
const cancelShareDone = () => {
    proxy.Confirm(`你确定要取消分享吗？`, async () => {
        let result = await proxy.Request({
            url: api.cancelShare,
            params: {
                shareIds: cancelShareIdList.value.join(",")
            }
        })
        if (!result) {
            return
        }
        proxy.Message.success("取消分享成功！")
        await loadDataList()
    })
}
const columns = [
    {
        label: "文件名",
        prop: "fileName",
        scopedSlots: "fileName"
    },
    {
        label: "分享码",
        prop: "code",
        width: 100
    },
    {
        label: "分享时间",
        prop: "shareTime",
        width: 200
    },
    {
        label: "失效时间",
        prop: "expireTime",
        scopedSlots: "expireTime",
        width: 200
    },
    {
        label: "是否失效",
        prop: "isExp",
        scopedSlots: "isExp",
        width: 100
    },
    {
        label: "浏览次数",
        prop: "showCount",
        width: 100
    }
]
</script>

<style lang="scss" scoped>
@import "@/assets/file.list.scss";

.file-list {
  margin-top: 10px;

  .file-item {
    .file-name {
      span {
        &:hover {
          color: #494944;
        }
      }
    }

    .op {
      width: 170px;
    }
  }
}
</style>
