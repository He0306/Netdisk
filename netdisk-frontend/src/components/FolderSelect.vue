<template>
    <div>
        <el-dialog width="500" title="移动" v-model="dialogFormVisible">
            <div class="navigation-panel"></div>
            <Navigation ref="navigationRef" @navChange="navChange" :watchPath="false"></Navigation>
            <div class="folder-list" v-if="folderList.length>0">
                <div class="folder-item" v-for="item in folderList" @click="selectFolder(item)">
                    <Icon :fileType="0"></Icon>
                    <span class="file-name">{{ item.fileName }}</span>
                </div>
            </div>
            <div v-else class="tips">
                <span style="color: red"> 移动到 </span>
                <span>{{ currentFolder.fileName }}</span>
            </div>
            <template #footer>
              <span class="dialog-footer">
                  <el-button type="primary" @click="folderSelect">移动到此</el-button>
              </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import {getCurrentInstance, ref} from "vue";
import Icon from "@/components/Icon.vue";

const {proxy} = getCurrentInstance()

const dialogFormVisible = ref(false)

const api = {
    loadAllFolder: "/file/loadAllFolder"
}

const currentFolder = ref({})
const currentFileIds = ref({})
const filePid = ref("0")
const folderList = ref([])

const showFolderDialog = (currentFolder) => {
    dialogFormVisible.value = true
    currentFileIds.value = currentFolder
    loadAllFolder()
}

const close = () => {
    dialogFormVisible.value = false
}
defineExpose({
    showFolderDialog,
    close
})
const loadAllFolder = async () => {
    let result = await proxy.Request({
        url: api.loadAllFolder,
        params: {
            filePid: filePid.value,
            currentFileIds: currentFileIds.value
        }
    })
    if (!result) {
        return;
    }
    folderList.value = result.data
}
// 提交
const emit = defineEmits(["folderSelect"])
const folderSelect = () => {
    emit("folderSelect", filePid.value)
}
// 选择目录
const navigationRef = ref()
const selectFolder = (data) => {
    navigationRef.value.openFolder(data)
}

// 导航改变回调
const navChange = (data) => {
    const {curFolder} = data
    currentFolder.value = curFolder
    filePid.value = curFolder.fileId
    loadAllFolder()
}
</script>

<style lang="scss" scoped>
.navigation-panel {
  padding-left: 10px;
  background: #f1f1f1;
}

.folder-list {
  .folder-item {
    cursor: pointer;
    display: flex;
    align-items: center;
    padding: 10px;

    .file-name {
      display: inline-block;
      margin-left: 10px;
    }

    &:hover {
      background: #f8f8f8;
    }
  }
    max-height: calc(100vh - 200px);
    min-height: 200px;
}

.tips {
  text-align: center;
  line-height: 200px;

  span {
    color: #06a7ff;
  }
}

</style>