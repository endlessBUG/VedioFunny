<template>
  <div class="deploy-model">
    <el-form label-width="120px">
      <el-form-item label="实例数量">
        <el-input-number v-model="form.instanceCount" :min="1" :max="5"></el-input-number>
      </el-form-item>
      <el-form-item label="最小工作节点">
        <el-input-number v-model="form.minWorkers" :min="1" :max="5"></el-input-number>
      </el-form-item>
      <el-form-item label="最大工作节点">
        <el-input-number v-model="form.maxWorkers" :min="1" :max="20"></el-input-number>
      </el-form-item>
      <el-form-item label="每节点CPU核心">
        <el-slider
          v-model="form.numCpus"
          :min="1"
          :max="32"
          show-input
          input-size="small"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="每节点GPU数量">
        <el-input-number v-model="form.numGpus" :min="0" :max="8"></el-input-number>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const form = ref({
  instanceCount: 1,
  minWorkers: 1,
  maxWorkers: 5,
  numCpus: 4,
  numGpus: 0,
});
</script>

<style scoped>
/* ... existing code ... */
</style>