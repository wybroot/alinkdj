<template>
  <div class="dj-root" :class="{ 'big-screen-active': isBigScreenMode }">
    <!-- ========================================================================= -->
    <!-- 普通业务端界面                                                            -->
    <!-- ========================================================================= -->
    <div v-if="!isBigScreenMode" class="dj-app">
      <!-- 顶部导航栏 -->
      <header class="dj-header">
        <div class="header-left">
          <div class="logo-badge">
            <el-icon :size="24"><Flag /></el-icon>
          </div>
          <div class="title-group">
            <h1>红河数据产业集团 · 智慧党建云平台</h1>
            <span class="sub-title">发展党员全生命周期管理系统（红河数据产业集团有限公司党总支）</span>
          </div>
        </div>

        <div class="header-right">
          <!-- 切换大屏按钮 -->
          <el-button 
            type="warning" 
            effect="dark" 
            icon="DataBoard" 
            class="big-screen-btn"
            @click="isBigScreenMode = true"
          >
            进入党建指挥大屏
          </el-button>

          <div class="role-switcher">
            <span class="role-label">当前角色：</span>
            <el-select v-model="currentRole" size="small" style="width: 230px">
              <el-option label="党总支组织员 (集团组织科)" value="committee_organizer" />
              <el-option label="支部书记 (红数信息技术支部)" value="branch_secretary" />
              <el-option label="支部书记 (云南幂次科技支部)" value="branch_secretary_mc" />
              <el-option label="支部书记 (红河链达科技支部)" value="branch_secretary_ld" />
              <el-option label="纪检监察主管 (廉洁把关)" value="discipline_inspector" />
              <el-option label="发展成员本人 (工号HH-HS-012)" value="member_self" />
            </el-select>
          </div>
          <el-tag type="danger" effect="dark" round class="org-tag">
            <el-icon><OfficeBuilding /></el-icon> 集团党总支
          </el-tag>
        </div>
      </header>

      <!-- 跑马灯合规警报条 -->
      <div class="compliance-marquee">
        <div class="marquee-tag">
          <el-icon><BellFilled /></el-icon>
          <span>党务合规雷达</span>
        </div>
        <div class="marquee-content">
          <div class="alert-item alert-danger">
            <el-tag size="small" type="danger">硬阻断</el-tag>
            <span>【张强·红数科技】积极分子考察仅 290 天（未满 365 天），系统强制锁定禁止提前流转至第 9 步发展对象！</span>
          </div>
          <div class="alert-item alert-warning">
            <el-tag size="small" type="warning">临期预警</el-tag>
            <span>【陈思佳·集团总部】申请书递交已第 22 天，距离“1个月内必须完成支部谈话”红线仅剩 8 天！</span>
          </div>
          <div class="alert-item alert-warning">
            <el-tag size="small" type="warning">转正提醒</el-tag>
            <span>【李晓辉·链达科技】预备期将在 14 天后满 1 年，已自动下发《转正申请书》催办指令。</span>
          </div>
          <div class="alert-item alert-info">
            <el-tag size="small" type="info">纪检联动</el-tag>
            <span>【林雨涵·幂次科技】政审函调完成，当前流转至集团纪委出具《廉洁从业意见书》。</span>
          </div>
        </div>
      </div>

      <!-- 主体内容区域 -->
      <main class="dj-main-container">
        <el-tabs v-model="activeTab" class="dj-nav-tabs">
          <!-- 标签页 1: 发展党员全景工作台 -->
          <el-tab-pane name="workbench">
            <template #label>
              <span class="tab-label"><el-icon><Operation /></el-icon> 发展党员全景工作台</span>
            </template>

            <!-- 5 大阶段快捷流转横幅 -->
            <div class="stages-overview-card">
              <div class="card-header-row">
                <h3><el-icon><TrendCharts /></el-icon> 发展党员 5 大阶段全流程流转航标</h3>
                <span class="rule-hint">《中国共产党发展党员工作细则》严格规定之 25 个环扣步骤</span>
              </div>
              <div class="stages-stepper">
                <div 
                  v-for="stage in STAGES_AND_STEPS" 
                  :key="stage.stageId"
                  class="stage-step-item"
                  :class="{ 
                    'active-stage': selectedFilterStage === stage.stageId,
                    'has-members': getStageMemberCount(stage.stageId) > 0 
                  }"
                  @click="filterByStage(stage.stageId)"
                >
                  <div class="stage-index-num">0{{ stage.stageId }}</div>
                  <div class="stage-info">
                    <div class="stage-title">{{ stage.stageName }}</div>
                    <div class="stage-meta">
                      <span>{{ stage.steps.length }} 个标准化步骤</span>
                      <el-badge :value="getStageMemberCount(stage.stageId)" class="member-count-badge" type="danger" />
                    </div>
                  </div>
                  <div class="stage-arrow"><el-icon><ArrowRight /></el-icon></div>
                </div>
              </div>
            </div>

            <!-- 发展成员台账列表与过滤区 -->
            <div class="members-table-card">
              <div class="table-toolbar">
                <div class="toolbar-left">
                  <el-input 
                    v-model="searchKeyword" 
                    placeholder="搜索发展成员姓名、工号、岗位..." 
                    prefix-icon="Search"
                    clearable
                    style="width: 250px"
                  />
                  <el-select v-model="filterBranch" placeholder="按子公司党支部筛选" clearable style="width: 250px">
                    <el-option label="全部所属党组织" value="" />
                    <el-option label="中共红河红数信息技术服务有限公司支部委员会" value="中共红河红数信息技术服务有限公司支部委员会" />
                    <el-option label="中共云南幂次科技有限公司支部委员会" value="中共云南幂次科技有限公司支部委员会" />
                    <el-option label="中共红河链达科技有限公司支部委员会" value="中共红河链达科技有限公司支部委员会" />
                    <el-option label="中共红河数据产业集团有限公司总支部委员会" value="中共红河数据产业集团有限公司总支部委员会" />
                  </el-select>
                  <el-select v-model="filterSpecialType" placeholder="国企骨干属性" clearable style="width: 160px">
                    <el-option label="全部骨干属性" value="" />
                    <el-option label="生产经营一线" value="frontline" />
                    <el-option label="高知/技术研发" value="technical" />
                    <el-option label="“双培养”骨干" value="dual" />
                  </el-select>
                  <el-button v-if="selectedFilterStage || filterBranch || filterSpecialType || searchKeyword" link type="primary" @click="resetFilters">
                    重置筛选
                  </el-button>
                </div>

                <div class="toolbar-right">
                  <el-button type="primary" icon="Plus" @click="openAddDialog">新建入党申请人建档</el-button>
                  <el-button icon="Download" @click="exportTableData">导出发展党员合规台账</el-button>
                </div>
              </div>

              <!-- 数据表格 -->
              <el-table :data="filteredMembers" style="width: 100%" stripe class="custom-dj-table">
                <el-table-column label="发展成员" min-width="170">
                  <template #default="{ row }">
                    <div class="member-cell">
                      <el-avatar :size="36" class="avatar-red">{{ row.name.slice(0, 1) }}</el-avatar>
                      <div class="member-meta">
                        <div class="name-line">
                          <strong>{{ row.name }}</strong>
                          <el-tag size="small" effect="plain" class="workno-tag">{{ row.workNo }}</el-tag>
                        </div>
                        <span class="sub-job">{{ row.jobTitle }}</span>
                      </div>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="所属党支部及部门" min-width="220">
                  <template #default="{ row }">
                    <div class="branch-cell">
                      <span class="branch-title">{{ row.branchName }}</span>
                      <span class="dept-title">{{ row.deptName }}</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="国企骨干标签" width="180">
                  <template #default="{ row }">
                    <div class="tags-wrapper">
                      <el-tag v-if="row.isFrontline" size="small" type="success">数据一线</el-tag>
                      <el-tag v-if="row.isTechnicalTalent" size="small" type="primary">科研骨干</el-tag>
                      <el-tag v-if="row.isDualCultivate" size="small" type="warning" effect="dark">双培养</el-tag>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="当前阶段与步骤" min-width="220">
                  <template #default="{ row }">
                    <div class="step-cell">
                      <el-tag :type="getStageTagType(row.currentStageId)" effect="light" class="stage-tag">
                        第 {{ row.currentStageId }} 阶段
                      </el-tag>
                      <div class="step-name">
                        <strong>第 {{ row.currentStepId }} 步：{{ getStepName(row.currentStepId) }}</strong>
                      </div>
                      <span class="stay-days">本步骤已停留 {{ row.daysInCurrentStep }} 天</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="党务合规状态" min-width="190">
                  <template #default="{ row }">
                    <div class="compliance-cell">
                      <el-tooltip :content="row.complianceAlert ? row.complianceAlert.message : '状态正常'" placement="top">
                        <el-tag :type="row.complianceAlert ? row.complianceAlert.type : 'success'" effect="plain" class="status-tag">
                          <el-icon v-if="row.complianceAlert && row.complianceAlert.type === 'danger'"><CircleCloseFilled /></el-icon>
                          <el-icon v-else-if="row.complianceAlert && row.complianceAlert.type === 'warning'"><WarningFilled /></el-icon>
                          <el-icon v-else><CircleCheckFilled /></el-icon>
                          {{ getComplianceShortText(row.complianceAlert) }}
                        </el-tag>
                      </el-tooltip>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="操作" width="220" fixed="right">
                  <template #default="{ row }">
                    <el-button type="primary" size="small" icon="Document" @click="openMemberDrawer(row)">
                      全景档案
                    </el-button>
                    <el-button size="small" icon="Right" @click="quickProgressStep(row)">
                      办理流转
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <!-- 标签页 2: 所有党员花名册 (全新模块) -->
          <el-tab-pane name="roster">
            <template #label>
              <span class="tab-label"><el-icon><User /></el-icon> 全集团所有党员花名册</span>
            </template>

            <div class="roster-container-card">
              <!-- 花名册统计头部 -->
              <div class="roster-stats-banner">
                <div class="stat-pill">
                  <span class="pill-label">在册党员总数</span>
                  <span class="pill-val">{{ rosterList.length }} 人</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">正式党员</span>
                  <span class="pill-val color-red">{{ getRosterCountByStatus(1) }} 人</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">预备党员</span>
                  <span class="pill-val color-orange">{{ getRosterCountByStatus(2) }} 人</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">发展对象/积极分子</span>
                  <span class="pill-val color-blue">{{ getRosterCountByStatus(3) + getRosterCountByStatus(4) }} 人</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">一线与研发骨干率</span>
                  <span class="pill-val color-green">85.7%</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">党费缴纳正常率</span>
                  <span class="pill-val color-green">100%</span>
                </div>
              </div>

              <!-- 搜索与筛选工具栏 -->
              <div class="table-toolbar">
                <div class="toolbar-left">
                  <el-input 
                    v-model="rosterSearchKeyword" 
                    placeholder="搜索姓名、工号、党内职务或部门..." 
                    prefix-icon="Search"
                    clearable
                    style="width: 250px"
                  />
                  <el-select v-model="rosterBranchFilter" placeholder="按子公司党支部" clearable style="width: 240px">
                    <el-option label="全部所属党组织" value="" />
                    <el-option label="红数信息支部" value="中共红河红数信息技术服务有限公司支部委员会" />
                    <el-option label="幂次科技支部" value="中共云南幂次科技有限公司支部委员会" />
                    <el-option label="链达科技支部" value="中共红河链达科技有限公司支部委员会" />
                    <el-option label="集团党总支" value="中共红河数据产业集团有限公司总支部委员会" />
                  </el-select>
                  <el-select v-model="rosterStatusFilter" placeholder="政治面貌/状态" clearable style="width: 150px">
                    <el-option label="全部政治面貌" value="" />
                    <el-option label="正式党员" :value="1" />
                    <el-option label="预备党员" :value="2" />
                    <el-option label="发展对象" :value="3" />
                    <el-option label="入党积极分子" :value="4" />
                    <el-option label="入党申请人" :value="5" />
                  </el-select>
                  <el-button v-if="rosterSearchKeyword || rosterBranchFilter || rosterStatusFilter" link type="primary" @click="resetRosterFilters">
                    重置
                  </el-button>
                </div>
                <div class="toolbar-right">
                  <el-button type="primary" icon="Plus" @click="openAddMemberDialog">新增党员</el-button>
                  <el-button type="warning" icon="Upload" @click="openImportRosterDialog">批量导入花名册</el-button>
                  <el-button icon="Download" @click="exportRosterExcel">导出花名册</el-button>
                </div>
              </div>

              <!-- 花名册数据表格 -->
              <el-table :data="filteredRosterList" style="width: 100%" stripe class="custom-dj-table">
                <el-table-column label="姓名" min-width="140">
                  <template #default="{ row }">
                    <div class="roster-name-cell">
                      <el-avatar :size="32" class="avatar-red">{{ row.name.slice(0, 1) }}</el-avatar>
                      <div>
                        <strong>{{ row.name }}</strong>
                        <div class="sub-workno">{{ row.workNo }}</div>
                      </div>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="性别/年龄" width="100">
                  <template #default="{ row }">
                    <span>{{ row.gender }} / {{ row.age }}岁</span>
                  </template>
                </el-table-column>

                <el-table-column label="政治面貌" width="130">
                  <template #default="{ row }">
                    <el-tag :type="getRosterPartyStatusTag(row.partyStatus)" effect="dark">
                      {{ getRosterPartyStatusText(row.partyStatus) }}
                    </el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="党内职务" width="140">
                  <template #default="{ row }">
                    <el-tag size="small" type="info" effect="plain">{{ row.partyPost }}</el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="党龄" width="100">
                  <template #default="{ row }">
                    <strong v-if="row.partyStatus === 1" class="standing-text">{{ calculatePartyStandingYears(row.joinPartyDate) }} 年</strong>
                    <span v-else class="text-muted">考察中</span>
                  </template>
                </el-table-column>

                <el-table-column label="入党/转正时间" width="150">
                  <template #default="{ row }">
                    <div class="date-col">
                      <span v-if="row.joinPartyDate">入党：{{ row.joinPartyDate }}</span>
                      <span v-if="row.officialPartyDate" class="text-muted">转正：{{ row.officialPartyDate }}</span>
                      <span v-if="!row.joinPartyDate" class="text-muted">待吸收发展</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="原所在党支部" min-width="190">
                  <template #default="{ row }">
                    <span v-if="row.originBranch" style="font-size: 12px; color: #475569">{{ row.originBranch }}</span>
                    <span v-else style="font-size: 11px; color: #94a3b8">本支部原生发展</span>
                  </template>
                </el-table-column>

                <el-table-column label="现所在党组织及职务" min-width="240">
                  <template #default="{ row }">
                    <div class="branch-cell">
                      <span class="branch-title">{{ row.branchName }}</span>
                      <span class="dept-title">{{ row.deptName }} · {{ row.jobTitle }}</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="骨干特色" width="170">
                  <template #default="{ row }">
                    <div class="tags-wrapper">
                      <el-tag v-if="row.isFrontline" size="small" type="success">生产一线</el-tag>
                      <el-tag v-if="row.isTechnicalTalent" size="small" type="primary">数字骨干</el-tag>
                      <el-tag v-if="row.isDualCultivate" size="small" type="warning">双培养</el-tag>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="党费缴纳" width="110">
                  <template #default="{ row }">
                    <el-tag size="small" type="success" effect="plain">
                      <el-icon><Check /></el-icon> 按月正常
                    </el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="年度集中培训时长" width="150">
                  <template #default="{ row }">
                    <div style="display: flex; flex-direction: column; gap: 2px">
                      <el-tag size="small" :type="(row.studyHours || 0) >= (row.studyTarget || 40) ? 'success' : 'warning'">
                        {{ row.studyHours || 0 }} / {{ row.studyTarget || 40 }} 学时
                      </el-tag>
                      <span style="font-size: 10.5px; color: #909399">{{ (row.studyHours || 0) >= (row.studyTarget || 40) ? '培训已达标' : '待集中补训' }}</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="全国党员编码" width="180">
                  <template #default="{ row }">
                    <span class="code-font">{{ row.nationalCode }}</span>
                  </template>
                </el-table-column>

                <el-table-column label="转入本支部时间" width="130">
                  <template #default="{ row }">
                    <span v-if="row.transferInDate" style="font-size: 12px; color: #303133">{{ row.transferInDate }}</span>
                    <span v-else style="font-size: 11px; color: #909399">建党在册</span>
                  </template>
                </el-table-column>

                <el-table-column label="组织关系转出" width="170">
                  <template #default="{ row }">
                    <div v-if="row.transferOutDate" style="display: flex; flex-direction: column; font-size: 11px">
                      <el-tag size="small" type="danger" effect="plain">已转出至：{{ row.transferOutBranch || '外单位党组织' }}</el-tag>
                      <span style="color: #909399; font-size: 10.5px">转出日期：{{ row.transferOutDate }}</span>
                    </div>
                    <el-tag v-else size="small" type="success" effect="plain">在册正常</el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="操作" width="110" fixed="right">
                  <template #default="{ row }">
                    <el-button type="primary" size="small" icon="Edit" @click="openEditMemberDialog(row)">
                      修改档案
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <!-- 标签页 3: 支部“三会一课”与组织生活台账 (全新闭环业务模块) -->
          <el-tab-pane name="meetings">
            <template #label>
              <span class="tab-label"><el-icon><Calendar /></el-icon> 支部“三会一课”与组织生活台账</span>
            </template>

            <div class="meetings-mgr-container">
              <!-- 顶部三会一课统计卡片 -->
              <div class="roster-stats-banner">
                <div class="stat-pill">
                  <span class="pill-label">支部委员会 (支委会)</span>
                  <span class="pill-val color-red">36 / 36 次</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">支部党员大会</span>
                  <span class="pill-val color-orange">12 / 12 次</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">专题党课</span>
                  <span class="pill-val color-blue">12 / 12 次</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">每月主题党日</span>
                  <span class="pill-val color-gold">36 / 36 期</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">平均到会出席率</span>
                  <span class="pill-val color-green">98.8%</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">纪要归档完整率</span>
                  <span class="pill-val color-green">100%</span>
                </div>
              </div>

              <!-- 搜索与筛选工具栏 (支持单选、多选与标签精准筛选) -->
              <div class="table-toolbar">
                <div class="toolbar-left">
                  <el-input 
                    v-model="meetingKeyword" 
                    placeholder="搜索会议议题、主持人、纪实..." 
                    prefix-icon="Search"
                    clearable
                    style="width: 220px"
                  />
                  <!-- 支持单选/多选所属党组织 -->
                  <el-select 
                    v-model="meetingBranchFilter" 
                    multiple 
                    collapse-tags 
                    collapse-tags-max="1" 
                    placeholder="党组织筛选(支持多选/单选)" 
                    clearable 
                    style="width: 230px"
                  >
                    <el-option label="红数信息支部 (云服务/网络安全)" value="中共红河红数信息技术服务有限公司支部委员会" />
                    <el-option label="幂次科技支部 (软件开发/数字化)" value="中共云南幂次科技有限公司支部委员会" />
                    <el-option label="链达科技支部 (城市综合体运营)" value="中共红河链达科技有限公司支部委员会" />
                    <el-option label="集团党总支" value="中共红河数据产业集团有限公司总支部委员会" />
                  </el-select>
                  <!-- 支持单选/多选组织生活类型 -->
                  <el-select 
                    v-model="meetingTypeFilter" 
                    multiple 
                    collapse-tags 
                    collapse-tags-max="1" 
                    placeholder="组织生活类型(支持多选/单选)" 
                    clearable 
                    style="width: 210px"
                  >
                    <el-option label="支委会" :value="1" />
                    <el-option label="支部党员大会" :value="2" />
                    <el-option label="专题党课" :value="3" />
                    <el-option label="主题党日" :value="4" />
                  </el-select>
                  <!-- 新增：支持通过议题彩色标签单选/多选筛选 -->
                  <el-select 
                    v-model="meetingTagFilter" 
                    multiple 
                    collapse-tags 
                    collapse-tags-max="1" 
                    placeholder="议题标签筛选(支持多选/单选)" 
                    clearable 
                    style="width: 210px"
                  >
                    <el-option 
                      v-for="tag in availableTopicTags" 
                      :key="tag.id" 
                      :label="tag.name" 
                      :value="tag.name"
                    >
                      <div style="display: flex; align-items: center; gap: 6px">
                        <span :style="{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: tag.color }"></span>
                        <span>{{ tag.name }}</span>
                      </div>
                    </el-option>
                  </el-select>
                  <el-button v-if="meetingKeyword || (meetingBranchFilter && meetingBranchFilter.length) || (meetingTypeFilter && meetingTypeFilter.length) || (meetingTagFilter && meetingTagFilter.length)" link type="primary" @click="resetMeetingFilters">
                    重置筛选
                  </el-button>
                </div>

                <div class="toolbar-right">
                  <el-button type="primary" icon="Plus" @click="openAddMeetingDialog">记录组织生活会议</el-button>
                  <el-button type="warning" plain icon="CollectionTag" @click="openManageTagsDialog">管理议题标签</el-button>
                  <el-button icon="Download" @click="exportMeetingsExcel">导出组织生活台账</el-button>
                </div>
              </div>

              <!-- 会议台账表格 -->
              <el-table :data="filteredMeetingsList" style="width: 100%" stripe class="custom-dj-table">
                <el-table-column label="组织生活类型" width="130">
                  <template #default="{ row }">
                    <el-tag :type="getMeetingTypeTag(row.meetingType)" effect="dark">
                      {{ row.meetingTypeName }}
                    </el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="会议主要议题及重点研究事项" min-width="290">
                  <template #default="{ row }">
                    <div style="display: flex; flex-direction: column; gap: 4px">
                      <strong>{{ row.title }}</strong>
                      <!-- 重点研究议题与彩色标签 -->
                      <div v-if="row.agendaItems && row.agendaItems.length" style="display: flex; flex-direction: column; gap: 3px; margin: 2px 0">
                        <div v-for="(ag, agIdx) in row.agendaItems" :key="agIdx" style="display: flex; align-items: center; gap: 6px; font-size: 11.5px">
                          <el-tag size="small" :color="ag.tagColor" effect="dark" style="border: none; color: #fff; font-size: 10px; height: 18px; line-height: 18px; padding: 0 5px">
                            {{ ag.tagName }}
                          </el-tag>
                          <span style="color: #334155">{{ ag.topic }}</span>
                        </div>
                      </div>
                      <span style="font-size: 11px; color: #94a3b8; line-height: 1.4">{{ row.content }}</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="所属党组织" min-width="190">
                  <template #default="{ row }">
                    <div style="display: flex; flex-direction: column">
                      <span style="font-weight: 600; font-size: 12.5px">{{ row.branchShort }}</span>
                      <span style="font-size: 11px; color: #909399">{{ row.branchName }}</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="开会时间与地点" width="170">
                  <template #default="{ row }">
                    <div style="display: flex; flex-direction: column; font-size: 12px">
                      <span><el-icon><Calendar /></el-icon> {{ row.date }}</span>
                      <span style="color: #909399; font-size: 11px">{{ row.place }}</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="主持人 / 主讲人" width="140">
                  <template #default="{ row }">
                    <div style="font-size: 12px">
                      <div>{{ row.moderator }}</div>
                      <div v-if="row.speaker" style="color: #e6a23c; font-size: 11px">主讲：{{ row.speaker }}</div>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="参会考勤" width="130">
                  <template #default="{ row }">
                    <div style="display: flex; flex-direction: column; gap: 2px">
                      <span style="font-size: 12px">到会：<strong>{{ row.actualCount }}</strong> / {{ row.expectedCount }}人</span>
                      <el-progress :percentage="row.attendanceRate" :stroke-width="5" :show-text="false" color="#67c23a" />
                      <span style="font-size: 10px; color: #67c23a">到会率 {{ row.attendanceRate }}%</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="发展党员业务联动" width="170">
                  <template #default="{ row }">
                    <el-tag v-if="row.relatedStep > 0" type="danger" effect="plain" size="small">
                      联动第 {{ row.relatedStep }} 步 ({{ row.relatedMember }})
                    </el-tag>
                    <span v-else style="color: #909399; font-size: 11.5px">常规党内生活</span>
                  </template>
                </el-table-column>

                <el-table-column label="操作与附件归档" width="280" fixed="right">
                  <template #default="{ row }">
                    <div style="display: flex; gap: 6px; align-items: center">
                      <el-button type="primary" size="small" icon="Edit" @click="openEditMeetingDialog(row)">
                        修改
                      </el-button>
                      <el-button size="small" icon="Paperclip" @click="openMeetingAttachmentsDialog(row)">
                        附件 ({{ (row.attachments && row.attachments.length) || 3 }}件)
                      </el-button>
                      <el-button link type="success" size="small" icon="Download" @click="downloadMeetingDoc(row)">
                        下载纪要
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <!-- 标签页 4: 组织与个人奖惩/荣誉台账 (全新专题) -->
          <el-tab-pane name="honors">
            <template #label>
              <span class="tab-label"><el-icon><Trophy /></el-icon> 组织与个人奖惩/荣誉台账</span>
            </template>

            <div class="honors-container-card">
              <!-- 顶部奖惩荣誉统计卡片 -->
              <div class="roster-stats-banner">
                <div class="stat-pill">
                  <span class="pill-label">组织荣誉表彰</span>
                  <span class="pill-val color-red">{{ getHonorCount(2, 1) }} 项</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">个人荣誉表彰</span>
                  <span class="pill-val color-gold">{{ getHonorCount(1, 1) }} 项</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">纪律处分 / 诫勉批评</span>
                  <span class="pill-val color-orange">{{ getHonorCount(null, 2) }} 项</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">省部级及以上表彰</span>
                  <span class="pill-val color-red">2 项</span>
                </div>
                <div class="stat-pill">
                  <span class="pill-label">整改清零闭环率</span>
                  <span class="pill-val color-green">100%</span>
                </div>
              </div>

              <!-- 搜索与筛选工具栏 -->
              <div class="table-toolbar">
                <div class="toolbar-left">
                  <el-input 
                    v-model="honorKeyword" 
                    placeholder="搜索荣誉/处分名称、获得者、文号..." 
                    prefix-icon="Search"
                    clearable
                    style="width: 240px"
                  />
                  <!-- 奖惩分类筛选：组织 vs 个人 -->
                  <el-select v-model="honorCategoryFilter" placeholder="奖惩主体分类" clearable style="width: 170px">
                    <el-option label="全部主体分类" :value="null" />
                    <el-option label="组织奖惩/荣誉" :value="2" />
                    <el-option label="个人奖惩/荣誉" :value="1" />
                  </el-select>
                  <!-- 奖惩类型筛选：荣誉表彰 vs 处分惩戒 -->
                  <el-select v-model="honorRecordTypeFilter" placeholder="奖惩类型" clearable style="width: 160px">
                    <el-option label="全部奖惩类型" :value="null" />
                    <el-option label="荣誉表彰" :value="1" />
                    <el-option label="纪律处分/诫勉" :value="2" />
                  </el-select>
                  <!-- 所属党支部筛选 -->
                  <el-select v-model="honorBranchFilter" multiple collapse-tags collapse-tags-max="1" placeholder="党组织筛选(可多选)" clearable style="width: 220px">
                    <el-option label="红数信息支部" value="中共红河红数信息技术服务有限公司支部委员会" />
                    <el-option label="幂次科技支部" value="中共云南幂次科技有限公司支部委员会" />
                    <el-option label="链达科技支部" value="中共红河链达科技有限公司支部委员会" />
                    <el-option label="集团党总支" value="中共红河数据产业集团有限公司总支部委员会" />
                  </el-select>
                  <!-- 级别筛选 -->
                  <el-select v-model="honorLevelFilter" placeholder="表彰/处分级别" clearable style="width: 150px">
                    <el-option label="全部级别" value="" />
                    <el-option label="国家级" value="国家级" />
                    <el-option label="省部级" value="省部级" />
                    <el-option label="州级/市级" value="州级/市级" />
                    <el-option label="集团级" value="集团级" />
                    <el-option label="支部级" value="支部级" />
                  </el-select>
                  <el-button v-if="honorKeyword || honorCategoryFilter !== null || honorRecordTypeFilter !== null || (honorBranchFilter && honorBranchFilter.length) || honorLevelFilter" link type="primary" @click="resetHonorFilters">
                    重置筛选
                  </el-button>
                </div>

                <div class="toolbar-right">
                  <el-button type="primary" icon="Plus" @click="openAddHonorDialog">登记奖惩/荣誉</el-button>
                  <el-button icon="Download" @click="exportHonorsExcel">导出荣誉台账</el-button>
                </div>
              </div>

              <!-- 奖惩荣誉数据表格 -->
              <el-table :data="filteredHonorsList" style="width: 100%" stripe class="custom-dj-table">
                <el-table-column label="奖惩主体分类" width="140">
                  <template #default="{ row }">
                    <el-tag :type="row.category === 2 ? 'warning' : 'primary'" effect="dark">
                      <el-icon v-if="row.category === 2"><OfficeBuilding /></el-icon>
                      <el-icon v-else><User /></el-icon>
                      {{ row.category === 2 ? '组织荣誉/奖惩' : '个人荣誉/奖惩' }}
                    </el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="奖惩性质" width="130">
                  <template #default="{ row }">
                    <el-tag :type="row.recordType === 1 ? 'success' : 'danger'" effect="plain">
                      <el-icon v-if="row.recordType === 1"><Trophy /></el-icon>
                      <el-icon v-else><Warning /></el-icon>
                      {{ row.recordTypeName }}
                    </el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="表彰/处分名称及主要事迹" min-width="280">
                  <template #default="{ row }">
                    <div style="display: flex; flex-direction: column; gap: 4px">
                      <strong :style="{ color: row.recordType === 1 ? '#c21c1d' : '#e6a23c', fontSize: '13.5px' }">
                        {{ row.title }}
                      </strong>
                      <span style="font-size: 11.5px; color: #64748b; line-height: 1.4">{{ row.reasonContent }}</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="获奖/受处分主体" min-width="170">
                  <template #default="{ row }">
                    <div style="display: flex; align-items: center; gap: 6px">
                      <strong style="color: #1e293b">{{ row.targetName }}</strong>
                      <el-tag v-if="row.workNo" size="small" type="info">{{ row.workNo }}</el-tag>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="所属党组织" min-width="190">
                  <template #default="{ row }">
                    <div style="display: flex; flex-direction: column">
                      <span style="font-weight: 600; font-size: 12.5px">{{ row.orgShort }}</span>
                      <span style="font-size: 11px; color: #94a3b8">{{ row.orgName }}</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="表彰级别" width="110">
                  <template #default="{ row }">
                    <el-tag size="small" :type="getHonorLevelTag(row.level)">
                      {{ row.level }}
                    </el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="决定/授予单位" min-width="180" prop="grantOrg" show-overflow-tooltip />

                <el-table-column label="决定文号" width="160">
                  <template #default="{ row }">
                    <span class="code-font" style="font-size: 11.5px">{{ row.docNo }}</span>
                  </template>
                </el-table-column>

                <el-table-column label="决定日期" width="120" prop="recordDate" />

                <el-table-column label="操作" width="160" fixed="right">
                  <template #default="{ row }">
                    <el-button type="primary" size="small" icon="Edit" @click="openEditHonorDialog(row)">
                      修改
                    </el-button>
                    <el-button type="danger" size="small" icon="Delete" link @click="deleteHonorItem(row)">
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <!-- 标签页 5: 25步全景规范与文书套打指南（支持默认与管理员导入双轨制） -->
          <el-tab-pane name="templates">
            <template #label>
              <span class="tab-label"><el-icon><DocumentCopy /></el-icon> 25步文书模板管理（默认+导入）</span>
            </template>

            <div class="templates-mgr-container">
              <div class="mgr-header-card">
                <div class="mgr-header-left">
                  <h3><el-icon><FolderOpened /></el-icon> 发展党员 25 步全套文书模板管理库（双轨制支持）</h3>
                  <p>系统已内置全部 25 个步骤中组部官方标准模板；同时支持集团管理员按红河数据产业集团红头格式自主<strong>导入/更新自定义模板</strong>。套打时自动优先采用已启用的自定义模板，并支持一键恢复官方标准！</p>
                </div>
                <div class="mgr-header-right">
                  <el-tag type="success" effect="dark" size="large">内置官方默认库：25 份</el-tag>
                  <el-tag type="warning" effect="dark" size="large">已导入企业定制版：{{ customizedTemplatesCount }} 份</el-tag>
                </div>
              </div>

              <!-- 模板列表表格 -->
              <el-table :data="templatesList" style="width: 100%" stripe class="custom-dj-table">
                <el-table-column label="步骤编码" width="110">
                  <template #default="{ row }">
                    <el-tag size="small" type="danger" effect="dark">第 {{ row.stepId }} 步</el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="模板名称及标准范式" min-width="220">
                  <template #default="{ row }">
                    <div class="tpl-name-cell">
                      <strong>{{ row.name }}</strong>
                      <span class="tpl-code-text">{{ row.code }}</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="当前启用版本状态" width="180">
                  <template #default="{ row }">
                    <div class="tpl-status-cell">
                      <el-tag v-if="row.isCustomized" type="warning" effect="dark">
                        <el-icon><EditPen /></el-icon> 企业定制版
                      </el-tag>
                      <el-tag v-else type="info" effect="plain">
                        <el-icon><Document /></el-icon> 官方标准版
                      </el-tag>
                      <span class="version-tag">{{ row.version }}</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="当前文件说明" min-width="240">
                  <template #default="{ row }">
                    <div class="tpl-file-meta">
                      <span v-if="row.isCustomized" class="custom-file-name">
                        <el-icon><Files /></el-icon> {{ row.customName }}
                      </span>
                      <span v-else class="default-file-name">
                        <el-icon><Document /></el-icon> {{ row.defaultName }}
                      </span>
                      <span class="update-sub">{{ row.updateUser }} · {{ row.updateTime }}</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="模板维护与套打操作" width="340" fixed="right">
                  <template #default="{ row }">
                    <el-button size="small" link type="primary" icon="Download" @click="downloadCurrentTemplate(row)">
                      下载使用中模板
                    </el-button>
                    <el-button size="small" link type="info" icon="Document" @click="downloadDefaultTemplate(row)">
                      下载官方默认
                    </el-button>
                    <el-button size="small" link type="warning" icon="Upload" @click="openUploadDialog(row)">
                      管理员导入
                    </el-button>
                    <el-button 
                      v-if="row.isCustomized" 
                      size="small" 
                      link 
                      type="danger" 
                      icon="RefreshLeft" 
                      @click="restoreDefaultTemplate(row)"
                    >
                      恢复默认
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <!-- 标签页 4: 国企年度指标与结构驾驶舱 -->
          <el-tab-pane name="cockpit">
            <template #label>
              <span class="tab-label"><el-icon><DataAnalysis /></el-icon> 年度发展指标与结构驾驶舱</span>
            </template>

            <div class="cockpit-container">
              <!-- 统计指标四卡片 -->
              <div class="metrics-grid">
                <div v-for="(metric, idx) in ANNUAL_QUOTA.metrics" :key="idx" class="metric-card">
                  <div class="metric-card-top">
                    <span class="metric-title">{{ metric.label }}</span>
                    <el-tag size="small" type="success">达标</el-tag>
                  </div>
                  <div class="metric-numbers">
                    <span class="actual-val">{{ metric.actual }}%</span>
                    <span class="target-val">国资委标准 ≥ {{ metric.target }}%</span>
                  </div>
                  <el-progress 
                    :percentage="metric.actual" 
                    :color="idx === 3 ? '#e6a23c' : '#c21c1d'" 
                    :stroke-width="8" 
                    :show-text="false" 
                  />
                  <div class="metric-desc">{{ metric.desc }}</div>
                </div>
              </div>

              <!-- 图表与结构分析区 -->
              <div class="charts-row">
                <div class="chart-box">
                  <div class="chart-header">
                    <h4><el-icon><PieChart /></el-icon> 2025年度发展党员阶段分布漏斗</h4>
                    <span class="plan-stat">总发展规划：{{ ANNUAL_QUOTA.totalPlan }} 人 / 已完成：{{ ANNUAL_QUOTA.approvedCount }} 人</span>
                  </div>
                  <div class="funnel-container">
                    <div class="funnel-stage stage-1">
                      <span class="stage-lbl">① 申请入党人库</span>
                      <span class="stage-val">22 人 (蓄水池)</span>
                    </div>
                    <div class="funnel-stage stage-2">
                      <span class="stage-lbl">② 确定培养入党积极分子</span>
                      <span class="stage-val">14 人 (考察中)</span>
                    </div>
                    <div class="funnel-stage stage-3">
                      <span class="stage-lbl">③ 确定发展对象 (政审/培训)</span>
                      <span class="stage-val">6 人 (重点把关)</span>
                    </div>
                    <div class="funnel-stage stage-4">
                      <span class="stage-lbl">④ 预备党员接收 (大会讨论)</span>
                      <span class="stage-val">5 人 (待上总支会)</span>
                    </div>
                    <div class="funnel-stage stage-5">
                      <span class="stage-lbl">⑤ 预备党员转正 (归档)</span>
                      <span class="stage-val">7 人 (已按期转正)</span>
                    </div>
                  </div>
                </div>

                <div class="chart-box">
                  <div class="chart-header">
                    <h4><el-icon><Histogram /></el-icon> 三大子公司党支部发展进度与指标达标对比</h4>
                  </div>
                  <div class="branch-progress-list">
                    <div class="branch-item">
                      <div class="b-info">
                        <span>红数信息党支部 (云服务、运维服务、网络及安全服务)</span>
                        <strong>3 / 4 人 (75%)</strong>
                      </div>
                      <el-progress :percentage="75" color="#c21c1d" />
                    </div>
                    <div class="branch-item">
                      <div class="b-info">
                        <span>幂次科技党支部 (软件开发、平台运营、企业数字化转型支撑)</span>
                        <strong>5 / 5 人 (100% 满额)</strong>
                      </div>
                      <el-progress :percentage="100" color="#67c23a" />
                    </div>
                    <div class="branch-item">
                      <div class="b-info">
                        <span>链达科技党支部 (城市综合体运营等)</span>
                        <strong>3 / 4 人 (75%)</strong>
                      </div>
                      <el-progress :percentage="75" color="#e6a23c" />
                    </div>
                  </div>
                </div>
              </div>

              <!-- 国企“双培养”机制推进台账卡片 -->
              <div class="dual-cultivate-card">
                <div class="dual-header">
                  <div class="dual-title">
                    <el-icon :size="20" color="#c21c1d"><Medal /></el-icon>
                    <strong>国企“双培养”工程落地台账（把业务骨干培养成党员，把党员培养成业务骨干）</strong>
                  </div>
                  <el-tag type="danger" effect="plain">累计入库数字高素质骨干 10 人</el-tag>
                </div>
                <el-row :gutter="16" class="dual-row">
                  <el-col :span="8">
                    <div class="dual-stat-box">
                      <div class="num-text">6 人</div>
                      <div class="sub-text">政务云运维及大数据技术骨干递交入党申请并确立培养</div>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="dual-stat-box">
                      <div class="num-text">4 人</div>
                      <div class="sub-text">AI 算法科学家 / 博士研发团队带头人列入重点发展计划</div>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="dual-stat-box">
                      <div class="num-text">100%</div>
                      <div class="sub-text">指定“资深技术导师 + 党务骨干”双联系人指导考察</div>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </main>
    </div>

    <!-- ========================================================================= -->
    <!-- 智慧党建数字化大屏模式 (Big Screen Mode: 全屏科技红金态势感知)              -->
    <!-- ========================================================================= -->
    <BigScreenView v-else @close="isBigScreenMode = false" />

    <!-- ========================================================================= -->
    <!-- 模态弹窗区                                                                -->
    <!-- ========================================================================= -->
    <!-- 一人一档全景档案抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="currentMember ? `【一人一档全景电子档案】${currentMember.name}（工号：${currentMember.workNo}）` : '党员档案'"
      size="76%"
      direction="rtl"
      destroy-on-close
      class="member-profile-drawer"
    >
      <div v-if="currentMember" class="drawer-inner-layout">
        <!-- 顶部个人画像摘要卡 -->
        <div class="member-header-card">
          <div class="profile-left">
            <el-avatar :size="54" class="avatar-red-big">{{ currentMember.name.slice(0, 1) }}</el-avatar>
            <div class="profile-text">
              <div class="main-info">
                <h2>{{ currentMember.name }}</h2>
                <el-tag type="danger" effect="dark">{{ currentMember.gender }} / {{ currentMember.age }}岁</el-tag>
                <el-tag type="info">{{ currentMember.education }}</el-tag>
                <el-tag v-if="currentMember.isDualCultivate" type="warning" effect="dark">国企“双培养”骨干</el-tag>
              </div>
              <div class="dept-info">
                <span><el-icon><OfficeBuilding /></el-icon> {{ currentMember.deptName }}</span>
                <span><el-icon><UserFilled /></el-icon> 岗位职务：{{ currentMember.jobTitle }}</span>
                <span><el-icon><Connection /></el-icon> 党组织：{{ currentMember.branchName }}</span>
              </div>
            </div>
          </div>

          <div class="profile-right">
            <div class="cultivator-box">
              <span class="c-label">培养联系人 / 介绍人：</span>
              <strong>{{ currentMember.cultivators ? currentMember.cultivators.join('、') : '暂未指定' }}</strong>
            </div>
            <div class="current-state-box">
              <span class="c-label">当前流转状态：</span>
              <el-tag :type="getStageTagType(currentMember.currentStageId)" size="large" effect="dark">
                第 {{ currentMember.currentStageId }} 阶段 · 第 {{ currentMember.currentStepId }} 步：{{ getStepName(currentMember.currentStepId) }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 合规审计告警通知条 -->
        <el-alert
          v-if="currentMember.complianceAlert"
          :title="currentMember.complianceAlert.message"
          :type="currentMember.complianceAlert.type"
          show-icon
          :closable="false"
          class="drawer-compliance-alert"
        />

        <!-- 主体：左侧25步时间轴 + 右侧步骤详情与材料套打 -->
        <div class="drawer-split-body">
          <!-- 左侧：25步流转时钟轴 -->
          <div class="timeline-sidebar">
            <div class="sidebar-header">
              <h4><el-icon><Guide /></el-icon> 25个步骤流转进度树</h4>
              <span class="hint">点击各步骤查看档案材料</span>
            </div>
            <div class="steps-timeline-list">
              <div 
                v-for="step in all25StepsFlat" 
                :key="step.stepId"
                class="timeline-node"
                :class="{
                  'node-finished': step.stepId < currentMember.currentStepId,
                  'node-current': step.stepId === currentMember.currentStepId,
                  'node-future': step.stepId > currentMember.currentStepId,
                  'node-selected': selectedStepInDrawer === step.stepId
                }"
                @click="selectedStepInDrawer = step.stepId"
              >
                <div class="node-icon-col">
                  <span v-if="step.stepId < currentMember.currentStepId" class="icon-circle done">
                    <el-icon><Check /></el-icon>
                  </span>
                  <span v-else-if="step.stepId === currentMember.currentStepId" class="icon-circle current">
                    {{ step.stepId }}
                  </span>
                  <span v-else class="icon-circle future">
                    {{ step.stepId }}
                  </span>
                  <div class="line-segment"></div>
                </div>
                <div class="node-info-col">
                  <div class="node-title">第 {{ step.stepId }} 步：{{ step.name }}</div>
                  <div class="node-meta">
                    <span class="n-role">{{ step.role }}</span>
                    <span v-if="step.stepId === currentMember.currentStepId" class="pulse-tag">正在进行</span>
                    <span v-else-if="step.stepId < currentMember.currentStepId" class="done-tag">已办结</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 右侧：选定步骤材料库与审批操作台 -->
          <div class="step-detail-main">
            <div class="step-main-header">
              <div class="header-tit">
                <h3>第 {{ activeDrawerStepInfo.stepId }} 步：{{ activeDrawerStepInfo.name }}</h3>
                <el-tag size="small" type="danger" effect="plain">{{ activeDrawerStepInfo.role }}</el-tag>
              </div>
              <div class="rule-box">
                <strong>【政策依据与红线】</strong> {{ activeDrawerStepInfo.rule }}
              </div>
            </div>

            <!-- 国企合规审计雷达卡片 -->
            <div class="audit-radar-box" :class="activeDrawerStepAuditClass">
              <div class="radar-title">
                <el-icon><WarningFilled /></el-icon>
                <span>基层党建合规审计防错雷达检测结论</span>
              </div>
              <p class="radar-p">{{ activeDrawerStepAuditDesc }}</p>
            </div>

            <!-- 材料清单与智能套打区 -->
            <div class="materials-section">
              <div class="sec-header">
                <h4><el-icon><FolderChecked /></el-icon> 本步骤归档材料清单与套打</h4>
                <el-button type="primary" size="small" icon="Printer" @click="mockBatchExportDocs">
                  一键套打全套 Word/PDF 材料
                </el-button>
              </div>

              <el-table :data="stepMaterialsList" border style="width: 100%" size="small">
                <el-table-column label="材料名称" min-width="180">
                  <template #default="{ row }">
                    <span class="doc-name"><strong>{{ row.name }}</strong></span>
                  </template>
                </el-table-column>
                <el-table-column label="审核状态" width="120">
                  <template #default="{ row }">
                    <el-tag :type="getMaterialTagType(row.status)">
                      {{ getMaterialStatusText(row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="提交/审核时间" width="140" prop="time" />
                <el-table-column label="操作" width="180">
                  <template #default="{ row }">
                    <el-button link type="primary" size="small" icon="View" @click="previewMaterial(row)">预览</el-button>
                    <el-button link type="success" size="small" icon="Download" @click="downloadSingleDoc(row)">套打导出</el-button>
                    <el-button link type="warning" size="small" icon="Upload">重传</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <!-- 底部推进操作栏 -->
            <div class="step-footer-actions">
              <el-button @click="drawerVisible = false">关闭窗口</el-button>
              <el-button 
                type="primary" 
                icon="Check"
                @click="handleAdvanceStep"
              >
                确认审核并推进至下一步
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 管理员自定义导入模板弹窗 -->
    <el-dialog v-model="uploadDialogVisible" title="管理员自定义导入/替换模板 (.docx)" width="520px">
      <div v-if="selectedTemplateForUpload" class="upload-dialog-body">
        <el-alert
          title="导入说明：上传的企业定制模板将直接应用于本步骤的全套套打。系统将自动解析占位符并生效为当前版本；如格式异常可随时一键恢复系统官方默认模板！"
          type="info"
          :closable="false"
          style="margin-bottom: 16px"
        />
        <el-form label-width="110px">
          <el-form-item label="所属步骤">
            <strong>第 {{ selectedTemplateForUpload.stepId }} 步：{{ selectedTemplateForUpload.name }}</strong>
          </el-form-item>
          <el-form-item label="当前版本">
            <el-tag :type="selectedTemplateForUpload.isCustomized ? 'warning' : 'info'">{{ selectedTemplateForUpload.version }}</el-tag>
          </el-form-item>
          <el-form-item label="选择Word文件">
            <el-upload
              action="#"
              :auto-upload="false"
              :limit="1"
              :on-change="handleFileSelected"
              accept=".docx,.doc"
            >
              <el-button type="primary" icon="Upload">选择 .docx 文件</el-button>
              <template #tip>
                <div class="el-upload__tip">支持 Microsoft Word (.docx) 格式模板，大小不超过 30MB</div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" icon="Check" @click="confirmUploadCustomTemplate">确认导入并生效</el-button>
      </template>
    </el-dialog>

    <!-- 模拟套打材料预览弹窗 -->
    <el-dialog v-model="previewDialogVisible" title="党建规范材料预览及一键套打" width="600px">
      <div class="doc-preview-modal-body">
        <div class="doc-paper">
          <div class="doc-paper-header">
            <h3>{{ previewDocTitle }}</h3>
            <span class="doc-code">中国共产党发展党员规范档案 · 编号：HH-DATA-2025-0012</span>
          </div>
          <div class="doc-paper-content">
            <p><strong>被发展对象：</strong> {{ currentMember ? currentMember.name : '' }}（工号：{{ currentMember ? currentMember.workNo : '' }}）</p>
            <p><strong>所属党组织：</strong> {{ currentMember ? currentMember.branchName : '' }}</p>
            <p><strong>工作岗位及职务：</strong> {{ currentMember ? currentMember.jobTitle : '' }}</p>
            <p><strong>审查认定意见：</strong> 该同志政治觉悟高，在红河数据产业科研一线带头攻关、遵纪守法，符合《中国共产党发展党员工作细则》各项要求。</p>
            <div class="sign-seal-box">
              <div class="seal-mark">（党支部印章）</div>
              <div class="sign-line">党支部书记（签名）：李卫民</div>
              <div class="date-line">2025年03月20日</div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="previewDialogVisible = false">关闭</el-button>
        <el-button type="primary" icon="Download" @click="downloadDocSuccess">导出 Word 文档 (.docx)</el-button>
      </template>
    </el-dialog>

    <!-- 录入 / 修改党员信息弹窗 -->
    <el-dialog 
      v-model="addMemberDialogVisible" 
      :title="isEditingMember ? `【修改 / 完善党员档案】${newMemberForm.name || ''}` : '新增录入党员信息（建档入库）'" 
      width="680px"
    >
      <el-form :model="newMemberForm" label-width="120px" class="member-add-form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="党员姓名*" required>
              <el-input v-model="newMemberForm.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="员工工号*" required>
              <el-input v-model="newMemberForm.workNo" placeholder="如 HH-HS-088" :disabled="isEditingMember" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="身份证号*" required>
              <el-input v-model="newMemberForm.idCard" placeholder="18位公民身份证号" maxlength="18" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别 / 年龄">
              <div style="display: flex; gap: 8px">
                <el-select v-model="newMemberForm.gender" style="width: 100px">
                  <el-option label="男" value="男" />
                  <el-option label="女" value="女" />
                </el-select>
                <el-input-number v-model="newMemberForm.age" :min="18" :max="75" placeholder="年龄" style="flex: 1" />
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所属党支部*" required>
              <el-select v-model="newMemberForm.branchName" placeholder="请选择党支部" style="width: 100%">
                <el-option label="红数信息支部" value="中共红河红数信息技术服务有限公司支部委员会" />
                <el-option label="幂次科技支部" value="中共云南幂次科技有限公司支部委员会" />
                <el-option label="链达科技支部" value="中共红河链达科技有限公司支部委员会" />
                <el-option label="集团党总支" value="中共红河数据产业集团有限公司总支部委员会" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高学历">
              <el-select v-model="newMemberForm.education" placeholder="学历" style="width: 100%">
                <el-option label="大专" value="大专" />
                <el-option label="大学本科" value="大学本科" />
                <el-option label="硕士研究生" value="硕士研究生" />
                <el-option label="博士研究生" value="博士研究生" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="企业行政部门">
              <el-input v-model="newMemberForm.deptName" placeholder="如 云服务与安全运维部" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位职务">
              <el-input v-model="newMemberForm.jobTitle" placeholder="如 资深架构师 / 运营主管" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="政治面貌*" required>
              <el-select v-model="newMemberForm.partyStatus" placeholder="政治面貌" style="width: 100%">
                <el-option label="正式党员" :value="1" />
                <el-option label="预备党员" :value="2" />
                <el-option label="发展对象" :value="3" />
                <el-option label="入党积极分子" :value="4" />
                <el-option label="入党申请人" :value="5" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="党内职务">
              <el-select v-model="newMemberForm.partyPost" placeholder="党内职务" style="width: 100%">
                <el-option label="党总支书记" value="党总支书记" />
                <el-option label="支部书记" value="党支部书记" />
                <el-option label="支部副书记" value="支部副书记" />
                <el-option label="组织委员" value="支部组织委员" />
                <el-option label="宣传委员" value="支部宣传委员" />
                <el-option label="纪检委员" value="支部纪检委员" />
                <el-option label="党小组长" value="党小组长" />
                <el-option label="普通党员" value="普通党员" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16" v-if="newMemberForm.partyStatus === 1 || newMemberForm.partyStatus === 2">
          <el-col :span="12">
            <el-form-item label="入党时间">
              <el-date-picker v-model="newMemberForm.joinPartyDate" type="date" value-format="YYYY-MM-DD" placeholder="接收预备党员日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="党龄(自动计算)">
              <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 4px; padding: 6px 12px; font-weight: 700; color: #c21c1d; font-size: 13.5px">
                {{ calculatePartyStandingYears(newMemberForm.joinPartyDate) }} 年
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="原所在党支部">
              <el-input v-model="newMemberForm.originBranch" placeholder="转入前所在支部 / 原发展支部" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="转入本支部时间">
              <el-date-picker v-model="newMemberForm.transferInDate" type="date" value-format="YYYY-MM-DD" placeholder="转入支部日期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="年度集中培训时长">
              <el-input-number v-model="newMemberForm.studyHours" :min="0" :max="120" style="width: 100%" placeholder="目标40学时" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="转出本支部时间">
              <el-date-picker v-model="newMemberForm.transferOutDate" type="date" value-format="YYYY-MM-DD" placeholder="若无转出可留空" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16" v-if="newMemberForm.transferOutDate">
          <el-col :span="24">
            <el-form-item label="转出目标支部">
              <el-input v-model="newMemberForm.transferOutBranch" placeholder="如 转出至中共某党委/外单位" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="国企骨干标签">
          <el-checkbox v-model="newMemberForm.isFrontline">生产/业务一线骨干</el-checkbox>
          <el-checkbox v-model="newMemberForm.isTechnicalTalent">数字研发核心技术骨干</el-checkbox>
          <el-checkbox v-model="newMemberForm.isDualCultivate">列入“双培养”工程</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addMemberDialogVisible = false">取消</el-button>
        <el-button type="primary" icon="Check" @click="submitAddMember">
          {{ isEditingMember ? '保存修改档案' : '确认新增入库' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量导入党员花名册弹窗 -->
    <el-dialog v-model="importRosterDialogVisible" title="批量导入党员花名册 (Excel .xlsx)" width="680px">
      <div class="roster-import-dialog-body">
        <el-steps :active="importActiveStep" finish-status="success" simple style="margin-bottom: 20px">
          <el-step title="1.下载模板" icon="Download" />
          <el-step title="2.上传文件" icon="Upload" />
          <el-step title="3.校验入库" icon="Check" />
        </el-steps>

        <!-- 步骤1：下载模板 -->
        <div class="import-step-card">
          <div class="step-card-tit"><strong>第一步：下载标准导入模板</strong></div>
          <p class="step-card-desc">请先下载规范的 Excel 导入模板，按照格式填写现有党员及入党积极分子等信息（包含姓名、工号、身份证号、支部、政治面貌等）。</p>
          <el-button type="primary" icon="Download" @click="downloadRosterTemplateExcel">
            下载党员花名册导入模板 (.xlsx)
          </el-button>
        </div>

        <!-- 步骤2：上传文件 -->
        <div class="import-step-card" style="margin-top: 14px">
          <div class="step-card-tit"><strong>第二步：选择填好的 Excel 文件</strong></div>
          <el-upload
            action="#"
            :auto-upload="false"
            :limit="1"
            accept=".xlsx,.xls"
            :on-change="handleRosterExcelSelected"
          >
            <el-button type="warning" icon="Document">选择已填写的 Excel 文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持 .xlsx / .xls 格式，每次导入建议不超过 200 条记录</div>
            </template>
          </el-upload>
        </div>

        <!-- 步骤3：解析预览与合规检测 -->
        <div v-if="parsedRosterPreviewList.length > 0" class="import-preview-box" style="margin-top: 14px">
          <div class="preview-header">
            <el-tag type="success">
              <el-icon><Check /></el-icon> 成功解析 {{ parsedRosterPreviewList.length }} 条党员数据（无工号与身份证冲突）
            </el-tag>
          </div>
          <el-table :data="parsedRosterPreviewList" size="small" border max-height="180" style="margin-top: 8px">
            <el-table-column prop="name" label="姓名" width="90" />
            <el-table-column prop="workNo" label="工号" width="110" />
            <el-table-column prop="branchName" label="所属党组织" min-width="160" show-overflow-tooltip />
            <el-table-column prop="partyPost" label="职务" width="100" />
            <el-table-column label="政治面貌" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="getRosterPartyStatusTag(row.partyStatus)">{{ getRosterPartyStatusText(row.partyStatus) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="importRosterDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          icon="Check" 
          :disabled="parsedRosterPreviewList.length === 0" 
          @click="confirmBatchImportRoster"
        >
          确认批量导入并入库 ({{ parsedRosterPreviewList.length }}人)
        </el-button>
      </template>
    </el-dialog>

    <!-- 记录/修改组织生活会议弹窗 -->
    <el-dialog 
      v-model="addMeetingDialogVisible" 
      :title="isEditingMeeting ? `【修改 / 完善会议记录】${newMeetingForm.title || ''}` : '发起 / 记录支部“三会一课”与主题党日'" 
      width="680px"
    >
      <el-form :model="newMeetingForm" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所属党支部*" required>
              <el-select v-model="newMeetingForm.branchName" style="width: 100%">
                <el-option label="红数信息支部 (云服务/安全)" value="中共红河红数信息技术服务有限公司支部委员会" />
                <el-option label="幂次科技支部 (软件开发/数字化)" value="中共云南幂次科技有限公司支部委员会" />
                <el-option label="链达科技支部 (城市综合体运营)" value="中共红河链达科技有限公司支部委员会" />
                <el-option label="集团党总支" value="中共红河数据产业集团有限公司总支部委员会" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组织生活类型*" required>
              <el-select v-model="newMeetingForm.meetingType" style="width: 100%">
                <el-option label="支委会 (每月至少1次)" :value="1" />
                <el-option label="支部党员大会 (每季度1次)" :value="2" />
                <el-option label="专题党课 (每季度1次)" :value="3" />
                <el-option label="每月主题党日" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="会议主要议题*" required>
          <el-input v-model="newMeetingForm.title" placeholder="如 讨论接收王建国同志为预备党员 / 筑牢网络安全底座主题党日" />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="召开日期*" required>
              <el-date-picker v-model="newMeetingForm.date" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开会地点">
              <el-input v-model="newMeetingForm.place" placeholder="如 党员活动室 / 综合体现场" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="主持人*">
              <el-input v-model="newMeetingForm.moderator" placeholder="如 李卫民 (支部书记)" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主讲人(党课)">
              <el-input v-model="newMeetingForm.speaker" placeholder="讲党课时填写" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="应到人数">
              <el-input-number v-model="newMeetingForm.expectedCount" :min="1" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实到人数">
              <el-input-number v-model="newMeetingForm.actualCount" :min="1" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="发展党员联动">
              <el-select v-model="newMeetingForm.relatedStep" placeholder="是否关联发展党员" style="width: 100%">
                <el-option label="常规组织生活 (无关联)" :value="0" />
                <el-option label="第4步: 支委会确定积极分子" :value="4" />
                <el-option label="第7步: 积极分子季度考察写实" :value="7" />
                <el-option label="第10步: 支委会确定发展对象" :value="10" />
                <el-option label="第18步: 支部大会讨论接收预备党员" :value="18" />
                <el-option label="第24步: 支部大会讨论预备党员转正" :value="24" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="newMeetingForm.relatedStep > 0">
            <el-form-item label="关联成员姓名">
              <el-input v-model="newMeetingForm.relatedMember" placeholder="被讨论党员姓名" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 会议主要研究议题 (支持动态添加文本框与彩色标签) -->
        <div class="agenda-items-section" style="margin-bottom: 16px; background: #fdfaf2; border: 1px solid #faecd8; border-radius: 6px; padding: 12px 14px">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px">
            <span style="font-weight: 700; font-size: 13px; color: #b88230">
              <el-icon><Tickets /></el-icon> 会议重点研究议题及分类标签 (支持动态增加)
            </span>
            <div style="display: flex; gap: 8px">
              <el-button size="small" type="primary" plain icon="Plus" @click="addAgendaItemRow">
                增加一项议题
              </el-button>
              <el-button size="small" type="warning" plain icon="CollectionTag" @click="openManageTagsDialog">
                管理标签
              </el-button>
            </div>
          </div>

          <div v-if="newMeetingForm.agendaItems.length === 0" style="font-size: 12px; color: #909399; text-align: center; padding: 10px 0">
            暂未添加重点研究议题，点击上方“增加一项议题”开始录入
          </div>

          <div v-else style="display: flex; flex-direction: column; gap: 8px">
            <div 
              v-for="(agenda, aIdx) in newMeetingForm.agendaItems" 
              :key="aIdx"
              style="display: flex; align-items: center; gap: 8px"
            >
              <span style="font-size: 12px; color: #64748b; white-space: nowrap">议题{{ aIdx + 1 }}:</span>
              <el-input 
                v-model="agenda.topic" 
                placeholder="请输入会议重点研究的议题事项..." 
                style="flex: 1" 
              />
              <el-select 
                v-model="agenda.tagId" 
                placeholder="选择标签" 
                style="width: 150px"
                @change="(val) => handleAgendaTagChange(agenda, val)"
              >
                <el-option 
                  v-for="tag in availableTopicTags" 
                  :key="tag.id" 
                  :label="tag.name" 
                  :value="tag.id"
                >
                  <div style="display: flex; align-items: center; gap: 6px">
                    <span :style="{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: tag.color }"></span>
                    <span>{{ tag.name }}</span>
                  </div>
                </el-option>
              </el-select>
              <el-tag v-if="agenda.tagName" :color="agenda.tagColor" effect="dark" style="border: none; color: #fff; font-size: 11px">
                {{ agenda.tagName }}
              </el-tag>
              <el-button type="danger" link icon="Delete" @click="removeAgendaItemRow(aIdx)"></el-button>
            </div>
          </div>
        </div>

        <el-form-item label="主要决议纪实">
          <el-input v-model="newMeetingForm.content" type="textarea" :rows="3" placeholder="简要记录会议讨论过程、投票结果与决议内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addMeetingDialogVisible = false">取消</el-button>
        <el-button type="primary" icon="Check" @click="submitAddMeeting">
          {{ isEditingMeeting ? '保存修改内容' : '保存会议台账' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 议题分类标签管理与管理员新增弹窗 (彩色标记) -->
    <el-dialog v-model="manageTagsDialogVisible" title="议题分类标签管理 (支持管理员自定义新增与颜色标记)" width="560px">
      <div style="display: flex; flex-direction: column; gap: 14px">
        <!-- 新增标签区 -->
        <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 12px 14px">
          <div style="font-weight: 700; font-size: 13px; color: #1e293b; margin-bottom: 8px">
            <el-icon><Plus /></el-icon> 新增自定义议题分类标签
          </div>
          <div style="display: flex; gap: 8px; align-items: center">
            <el-input v-model="newTagName" placeholder="输入标签名称，如 发展党员/党纪学习" style="flex: 1" />
            <el-color-picker v-model="newTagColor" :predefine="['#c21c1d', '#e6a23c', '#67c23a', '#409eff', '#8b5cf6', '#ec4899', '#f97316', '#06b6d4']" />
            <el-button type="primary" icon="Check" @click="submitAddNewTag">添加标签</el-button>
          </div>
        </div>

        <!-- 现有标签库 -->
        <div style="font-weight: 700; font-size: 13px; color: #334155">
          当前可用议题分类标签库 (共 {{ availableTopicTags.length }} 个)
        </div>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px; max-height: 240px; overflow-y: auto">
          <div 
            v-for="tag in availableTopicTags" 
            :key="tag.id"
            style="display: flex; justify-content: space-between; align-items: center; background: #fff; border: 1px solid #e2e8f0; border-radius: 4px; padding: 8px 12px"
          >
            <div style="display: flex; align-items: center; gap: 8px">
              <span :style="{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: tag.color }"></span>
              <el-tag :color="tag.color" effect="dark" style="border: none; color: #fff; font-size: 11px">
                {{ tag.name }}
              </el-tag>
            </div>
            <el-button type="danger" link icon="Close" size="small" @click="deleteTopicTag(tag.id)"></el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="manageTagsDialogVisible = false">完成</el-button>
      </template>
    </el-dialog>

    <!-- 三会一课全套规范附件管理弹窗 (通知、纪要、决议、签到表、现场照片) -->
    <el-dialog 
      v-model="meetingAttachmentsDialogVisible" 
      :title="currentMeetingForAttach ? `【会议规范附件归档】${currentMeetingForAttach.title}` : '会议附件管理'" 
      width="720px"
    >
      <div v-if="currentMeetingForAttach" class="meeting-attach-dialog-body">
        <el-alert
          title="归档规范：党组织召开支委会、党员大会、党课与主题党日必须完整留存【会议通知】、【会议纪要】、【决议书】与【签到表原件扫描件】，以备迎检巡视巡察合规抽查。"
          type="info"
          :closable="false"
          style="margin-bottom: 14px"
        />

        <!-- 上传新附件区 -->
        <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 12px 16px; margin-bottom: 16px">
          <div style="font-weight: 700; font-size: 13px; color: #303133; margin-bottom: 8px">
            <el-icon><Upload /></el-icon> 上传新增会议附件材料
          </div>
          <div style="display: flex; gap: 10px; align-items: center; flex-wrap: wrap">
            <el-select v-model="uploadMeetingAttachType" placeholder="选择附件类别" style="width: 170px">
              <el-option label="会议通知" value="NOTICE" />
              <el-option label="会议纪要" value="MINUTES" />
              <el-option label="表决决议书" value="RESOLUTION" />
              <el-option label="签到考勤表" value="SIGNIN" />
              <el-option label="现场纪实照片" value="PHOTO" />
            </el-select>
            <el-upload
              action="#"
              :auto-upload="false"
              :limit="1"
              accept=".docx,.doc,.pdf,.jpg,.png"
              :on-change="handleMeetingAttachSelected"
            >
              <el-button icon="Folder">选择本地文件</el-button>
            </el-upload>
            <el-button type="primary" icon="Check" @click="confirmUploadMeetingAttach">确认上传归档</el-button>
          </div>
        </div>

        <!-- 已归档附件列表表格 -->
        <div style="font-weight: 700; font-size: 13.5px; color: #2c3e50; margin-bottom: 8px">
          已归档附件档案清单 ({{ currentMeetingAttachmentsList.length }} 件)
        </div>
        <el-table :data="currentMeetingAttachmentsList" border size="small" style="width: 100%">
          <el-table-column label="附件类别" width="130">
            <template #default="{ row }">
              <el-tag :type="getAttachCategoryTag(row.attachType)" size="small">
                {{ row.attachTypeName }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="文件名称" min-width="220" prop="fileName" show-overflow-tooltip />
          <el-table-column label="文件大小" width="100" prop="fileSize" />
          <el-table-column label="上传时间" width="140" prop="uploadTime" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" icon="Download" @click="downloadSingleMeetingAttach(row)">
                下载
              </el-button>
              <el-button link type="danger" size="small" icon="Delete" @click="deleteMeetingAttach(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button type="primary" @click="meetingAttachmentsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 登记/修改组织与个人奖惩荣誉弹窗 -->
    <el-dialog 
      v-model="honorDialogVisible" 
      :title="isEditingHonor ? `【修改奖惩/荣誉记录】${newHonorForm.title || ''}` : '登记组织或个人奖惩/荣誉'" 
      width="680px"
    >
      <el-form :model="newHonorForm" label-width="130px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="奖惩主体分类*" required>
              <el-radio-group v-model="newHonorForm.category" @change="handleHonorCategoryChange">
                <el-radio :label="2">组织奖惩/荣誉</el-radio>
                <el-radio :label="1">个人奖惩/荣誉</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="奖惩性质*" required>
              <el-radio-group v-model="newHonorForm.recordType">
                <el-radio :label="1">荣誉表彰</el-radio>
                <el-radio :label="2">纪律处分/诫勉</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="奖惩/表彰名称*" required>
          <el-input v-model="newHonorForm.title" placeholder="如 云南省国资委先进基层党组织 / 云南省数字技术工匠" />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="newHonorForm.category === 2 ? '获奖党支部*' : '党员姓名*'" required>
              <el-select 
                v-if="newHonorForm.category === 2" 
                v-model="newHonorForm.targetName" 
                placeholder="选择党支部" 
                style="width: 100%"
                @change="handleOrgTargetChange"
              >
                <el-option label="红数信息党支部" value="中共红河红数信息技术服务有限公司支部委员会" />
                <el-option label="幂次科技党支部" value="中共云南幂次科技有限公司支部委员会" />
                <el-option label="链达科技党支部" value="中共红河链达科技有限公司支部委员会" />
                <el-option label="集团党总支" value="中共红河数据产业集团有限公司总支部委员会" />
              </el-select>
              <el-select 
                v-else 
                v-model="newHonorForm.targetName" 
                filterable 
                placeholder="输入或选择党员" 
                style="width: 100%"
                @change="handleMemberTargetChange"
              >
                <el-option 
                  v-for="m in rosterList" 
                  :key="m.id" 
                  :label="`${m.name} (${m.workNo} - ${m.branchName.slice(0, 10)}...)`" 
                  :value="m.name" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属党组织*" required>
              <el-select v-model="newHonorForm.orgName" placeholder="所属党组织" style="width: 100%">
                <el-option label="红数信息支部" value="中共红河红数信息技术服务有限公司支部委员会" />
                <el-option label="幂次科技支部" value="中共云南幂次科技有限公司支部委员会" />
                <el-option label="链达科技支部" value="中共红河链达科技有限公司支部委员会" />
                <el-option label="集团党总支" value="中共红河数据产业集团有限公司总支部委员会" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="表彰/处分级别*" required>
              <el-select v-model="newHonorForm.level" placeholder="请选择级别" style="width: 100%">
                <el-option label="国家级" value="国家级" />
                <el-option label="省部级" value="省部级" />
                <el-option label="州级/市级" value="州级/市级" />
                <el-option label="集团级" value="集团级" />
                <el-option label="支部级" value="支部级" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="决定日期*" required>
              <el-date-picker v-model="newHonorForm.recordDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="授予/决定单位">
              <el-input v-model="newHonorForm.grantOrg" placeholder="如 云南省国资委党委 / 集团党总支" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="红头/处分文号">
              <el-input v-model="newHonorForm.docNo" placeholder="如 云国资党委发〔2024〕18号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item :label="newHonorForm.recordType === 1 ? '主要表彰事迹' : '处分事实与原因'">
          <el-input v-model="newHonorForm.reasonContent" type="textarea" :rows="3" placeholder="详细填写事迹说明、主要贡献或受诫勉处分的原因及整改要求" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="honorDialogVisible = false">取消</el-button>
        <el-button type="primary" icon="Check" @click="submitSaveHonor">
          {{ isEditingHonor ? '保存修改记录' : '确认登记入库' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import BigScreenView from './components/BigScreenView.vue'
import { 
  SOE_ORGS, 
  STAGES_AND_STEPS, 
  MOCK_MEMBERS, 
  ANNUAL_QUOTA, 
  ALL_MEMBERS_ROSTER, 
  TEMPLATES_CATALOG_25,
  MOCK_MEETING_RECORDS,
  AVAILABLE_TOPIC_TAGS,
  MOCK_HONOR_PUNISHMENT_LIST
} from './data/mockData.js'

// 基础模式与大屏状态
const isBigScreenMode = ref(false)
const currentRole = ref('committee_organizer')
const activeTab = ref('workbench')

// ==========================================
// 1. 发展党员工作台筛选
// ==========================================
const selectedFilterStage = ref(null)
const filterBranch = ref('')
const filterSpecialType = ref('')
const searchKeyword = ref('')

const filteredMembers = computed(() => {
  return MOCK_MEMBERS.filter(m => {
    if (selectedFilterStage.value && m.currentStageId !== selectedFilterStage.value) return false
    if (filterBranch.value && m.branchName !== filterBranch.value) return false
    if (filterSpecialType.value === 'frontline' && !m.isFrontline) return false
    if (filterSpecialType.value === 'technical' && !m.isTechnicalTalent) return false
    if (filterSpecialType.value === 'dual' && !m.isDualCultivate) return false
    if (searchKeyword.value) {
      const kw = searchKeyword.value.toLowerCase()
      const matchName = m.name.toLowerCase().includes(kw)
      const matchNo = m.workNo.toLowerCase().includes(kw)
      const matchDept = m.deptName.toLowerCase().includes(kw)
      const matchJob = m.jobTitle.toLowerCase().includes(kw)
      if (!matchName && !matchNo && !matchDept && !matchJob) return false
    }
    return true
  })
})

function getStageMemberCount(stageId) {
  return MOCK_MEMBERS.filter(m => m.currentStageId === stageId).length
}

function filterByStage(stageId) {
  selectedFilterStage.value = selectedFilterStage.value === stageId ? null : stageId
}

function resetFilters() {
  selectedFilterStage.value = null
  filterBranch.value = ''
  filterSpecialType.value = ''
  searchKeyword.value = ''
}

// ==========================================
// 2. 所有党员花名册逻辑与录入导入
// ==========================================
const rosterList = ref([...ALL_MEMBERS_ROSTER])
const rosterSearchKeyword = ref('')
const rosterBranchFilter = ref('')
const rosterStatusFilter = ref('')

const filteredRosterList = computed(() => {
  return rosterList.value.filter(m => {
    if (rosterBranchFilter.value && m.branchName !== rosterBranchFilter.value) return false
    if (rosterStatusFilter.value && m.partyStatus !== rosterStatusFilter.value) return false
    if (rosterSearchKeyword.value) {
      const kw = rosterSearchKeyword.value.toLowerCase()
      const matchName = m.name.toLowerCase().includes(kw)
      const matchNo = m.workNo.toLowerCase().includes(kw)
      const matchDept = m.deptName.toLowerCase().includes(kw)
      const matchPost = m.partyPost.toLowerCase().includes(kw)
      if (!matchName && !matchNo && !matchDept && !matchPost) return false
    }
    return true
  })
})

function getRosterCountByStatus(status) {
  return rosterList.value.filter(m => m.partyStatus === status).length
}

function getRosterPartyStatusTag(status) {
  const map = { 1: 'danger', 2: 'warning', 3: 'primary', 4: 'info', 5: '' }
  return map[status] || 'info'
}

function getRosterPartyStatusText(status) {
  const map = { 1: '正式党员', 2: '预备党员', 3: '发展对象', 4: '积极分子', 5: '入党申请人' }
  return map[status] || '未知'
}

function resetRosterFilters() {
  rosterSearchKeyword.value = ''
  rosterBranchFilter.value = ''
  rosterStatusFilter.value = ''
}

function exportRosterExcel() {
  ElMessage.success(`已成功导出【红河数据产业集团有限公司党总支全体在册党员花名册】(共 ${rosterList.value.length} 人，.xlsx 格式)`)
}

// 自动根据入党时间计算党龄 (年)
function calculatePartyStandingYears(joinDate) {
  if (!joinDate) return 0
  const join = new Date(joinDate)
  const now = new Date()
  let years = now.getFullYear() - join.getFullYear()
  if (now.getMonth() < join.getMonth() || (now.getMonth() === join.getMonth() && now.getDate() < join.getDate())) {
    years--
  }
  return Math.max(0, years)
}

// 单条新增与修改党员档案逻辑
const addMemberDialogVisible = ref(false)
const isEditingMember = ref(false)
const editingMemberId = ref(null)
const newMemberForm = ref({
  name: '',
  workNo: '',
  idCard: '',
  gender: '男',
  age: 30,
  branchName: '中共红河红数信息技术服务有限公司支部委员会',
  deptName: '',
  jobTitle: '',
  partyStatus: 1,
  partyPost: '普通党员',
  partyStandingYears: 0,
  joinPartyDate: '',
  officialPartyDate: '',
  nationalCode: '',
  studyHours: 40,
  studyTarget: 40,
  originBranch: '',
  transferInDate: '',
  transferOutDate: '',
  transferOutBranch: '',
  isFrontline: true,
  isTechnicalTalent: true,
  isDualCultivate: false
})

function openAddMemberDialog() {
  isEditingMember.value = false
  editingMemberId.value = null
  newMemberForm.value = {
    name: '',
    workNo: 'HH-' + (Math.floor(Math.random() * 890 + 100)),
    idCard: '',
    gender: '男',
    age: 30,
    branchName: '中共红河红数信息技术服务有限公司支部委员会',
    deptName: '',
    jobTitle: '',
    partyStatus: 1,
    partyPost: '普通党员',
    partyStandingYears: 0,
    joinPartyDate: '2023-06-20',
    officialPartyDate: '2024-06-20',
    nationalCode: '53250100' + Math.floor(Math.random() * 89999999 + 10000000),
    studyHours: 40,
    studyTarget: 40,
    originBranch: '',
    transferInDate: new Date().toISOString().slice(0, 10),
    transferOutDate: '',
    transferOutBranch: '',
    isFrontline: true,
    isTechnicalTalent: true,
    isDualCultivate: false
  }
  addMemberDialogVisible.value = true
}

function openEditMemberDialog(row) {
  isEditingMember.value = true
  editingMemberId.value = row.id
  newMemberForm.value = {
    name: row.name,
    workNo: row.workNo,
    idCard: row.idCard,
    gender: row.gender,
    age: row.age,
    branchName: row.branchName,
    deptName: row.deptName,
    jobTitle: row.jobTitle,
    partyStatus: row.partyStatus,
    partyPost: row.partyPost,
    partyStandingYears: calculatePartyStandingYears(row.joinPartyDate),
    joinPartyDate: row.joinPartyDate || '',
    officialPartyDate: row.officialPartyDate || '',
    nationalCode: row.nationalCode || '',
    studyHours: row.studyHours || 40,
    studyTarget: 40,
    originBranch: row.originBranch || '',
    transferInDate: row.transferInDate || '',
    transferOutDate: row.transferOutDate || '',
    transferOutBranch: row.transferOutBranch || '',
    isFrontline: !!row.isFrontline,
    isTechnicalTalent: !!row.isTechnicalTalent,
    isDualCultivate: !!row.isDualCultivate
  }
  addMemberDialogVisible.value = true
}

function submitAddMember() {
  if (!newMemberForm.value.name.trim()) {
    ElMessage.warning('请输入党员姓名！')
    return
  }
  if (!newMemberForm.value.workNo.trim()) {
    ElMessage.warning('请输入员工工号！')
    return
  }
  if (!newMemberForm.value.idCard.trim() || newMemberForm.value.idCard.length !== 18) {
    ElMessage.warning('请输入规范的 18 位身份证号码！')
    return
  }

  // 根据入党时间自动核算党龄
  const autoStandingYears = calculatePartyStandingYears(newMemberForm.value.joinPartyDate)

  if (isEditingMember.value) {
    // 修改已有党员档案
    const idx = rosterList.value.findIndex(m => m.id === editingMemberId.value)
    if (idx !== -1) {
      const item = rosterList.value[idx]
      item.name = newMemberForm.value.name.trim()
      item.idCard = newMemberForm.value.idCard.trim()
      item.gender = newMemberForm.value.gender
      item.age = newMemberForm.value.age
      item.branchName = newMemberForm.value.branchName
      item.deptName = newMemberForm.value.deptName || '业务部门'
      item.jobTitle = newMemberForm.value.jobTitle || '技术骨干'
      item.partyStatus = newMemberForm.value.partyStatus
      item.partyPost = newMemberForm.value.partyPost
      item.partyStandingYears = autoStandingYears
      item.joinPartyDate = newMemberForm.value.joinPartyDate || null
      item.officialPartyDate = newMemberForm.value.officialPartyDate || null
      item.studyHours = newMemberForm.value.studyHours || 40
      item.studyTarget = 40
      item.originBranch = newMemberForm.value.originBranch || ''
      item.transferInDate = newMemberForm.value.transferInDate || null
      item.transferOutDate = newMemberForm.value.transferOutDate || null
      item.transferOutBranch = newMemberForm.value.transferOutBranch || ''
      item.isFrontline = newMemberForm.value.isFrontline
      item.isTechnicalTalent = newMemberForm.value.isTechnicalTalent
      item.isDualCultivate = newMemberForm.value.isDualCultivate
    }
    addMemberDialogVisible.value = false
    ElMessage.success(`党员【${newMemberForm.value.name}】档案信息已成功修改更新！`)
  } else {
    // 新增录入
    const exists = rosterList.value.some(m => m.workNo === newMemberForm.value.workNo.trim())
    if (exists) {
      ElMessage.error(`员工工号 [${newMemberForm.value.workNo}] 已存在，请勿重复添加！`)
      return
    }

    rosterList.value.unshift({
      id: Date.now(),
      name: newMemberForm.value.name.trim(),
      workNo: newMemberForm.value.workNo.trim(),
      idCard: newMemberForm.value.idCard.trim(),
      gender: newMemberForm.value.gender,
      age: newMemberForm.value.age,
      branchName: newMemberForm.value.branchName,
      deptName: newMemberForm.value.deptName || '业务部门',
      jobTitle: newMemberForm.value.jobTitle || '技术骨干',
      partyStatus: newMemberForm.value.partyStatus,
      partyPost: newMemberForm.value.partyPost,
      partyStandingYears: autoStandingYears,
      joinPartyDate: newMemberForm.value.joinPartyDate || null,
      officialPartyDate: newMemberForm.value.officialPartyDate || null,
      duesStatus: 1,
      nationalCode: newMemberForm.value.nationalCode,
      studyHours: newMemberForm.value.studyHours || 40,
      studyTarget: 40,
      originBranch: newMemberForm.value.originBranch || '',
      transferInDate: newMemberForm.value.transferInDate || new Date().toISOString().slice(0, 10),
      transferOutDate: newMemberForm.value.transferOutDate || null,
      transferOutBranch: newMemberForm.value.transferOutBranch || '',
      isFrontline: newMemberForm.value.isFrontline,
      isTechnicalTalent: newMemberForm.value.isTechnicalTalent,
      isDualCultivate: newMemberForm.value.isDualCultivate
    })

    addMemberDialogVisible.value = false
    ElMessage.success(`成功录入新增党员【${newMemberForm.value.name}】！已实时纳入花名册档案。`)
  }
}

// 批量导入逻辑
const importRosterDialogVisible = ref(false)
const importActiveStep = ref(1)
const parsedRosterPreviewList = ref([])

function openImportRosterDialog() {
  importActiveStep.value = 1
  parsedRosterPreviewList.value = []
  importRosterDialogVisible.value = true
}

function downloadRosterTemplateExcel() {
  ElMessage.success('已下载《红河数据产业集团_党员花名册导入模板.xlsx》！')
  importActiveStep.value = 2
}

function handleRosterExcelSelected(file) {
  importActiveStep.value = 3
  parsedRosterPreviewList.value = [
    {
      id: Date.now() + 1,
      name: '何明华',
      workNo: 'HH-HS-042',
      gender: '男',
      age: 36,
      branchName: '中共红河红数信息技术服务有限公司支部委员会',
      deptName: '网络安全运行中心',
      jobTitle: '信息安全主管工程师',
      partyStatus: 1,
      partyPost: '党员',
      partyStandingYears: 8,
      joinPartyDate: '2017-06-25',
      officialPartyDate: '2018-06-25',
      duesStatus: 1,
      nationalCode: '532501001989062501',
      isFrontline: true,
      isTechnicalTalent: true,
      isDualCultivate: true
    },
    {
      id: Date.now() + 2,
      name: '许青青',
      workNo: 'HH-MC-058',
      gender: '女',
      age: 30,
      branchName: '中共云南幂次科技有限公司支部委员会',
      deptName: '数据工程部',
      jobTitle: '大数据清洗资深开发工程师',
      partyStatus: 1,
      partyPost: '党员',
      partyStandingYears: 4,
      joinPartyDate: '2021-07-01',
      officialPartyDate: '2022-07-01',
      duesStatus: 1,
      nationalCode: '532501001995070102',
      isFrontline: true,
      isTechnicalTalent: true,
      isDualCultivate: false
    },
    {
      id: Date.now() + 3,
      name: '段建军',
      workNo: 'HH-LD-023',
      gender: '男',
      age: 33,
      branchName: '中共红河链达科技有限公司支部委员会',
      deptName: '区块链工程部',
      jobTitle: '智能合约主任研发工程师',
      partyStatus: 2,
      partyPost: '预备党员',
      partyStandingYears: 0,
      joinPartyDate: '2024-10-18',
      officialPartyDate: null,
      duesStatus: 1,
      nationalCode: '532501001992101803',
      isFrontline: true,
      isTechnicalTalent: true,
      isDualCultivate: true
    },
    {
      id: Date.now() + 4,
      name: '杨丽萍',
      workNo: 'HH-JT-018',
      gender: '女',
      age: 27,
      branchName: '中共红河数据产业集团有限公司总支部委员会',
      deptName: '综合人力部',
      jobTitle: '党务人事主管',
      partyStatus: 1,
      partyPost: '党员',
      partyStandingYears: 3,
      joinPartyDate: '2022-05-12',
      officialPartyDate: '2023-05-12',
      duesStatus: 1,
      nationalCode: '532501001998051204',
      isFrontline: false,
      isTechnicalTalent: false,
      isDualCultivate: false
    }
  ]
  ElMessage.success(`已成功解析《${file.name}》，成功校验 4 条合规党员数据！`)
}

function confirmBatchImportRoster() {
  if (parsedRosterPreviewList.value.length === 0) return
  const count = parsedRosterPreviewList.value.length
  rosterList.value.unshift(...parsedRosterPreviewList.value)
  parsedRosterPreviewList.value = []
  importRosterDialogVisible.value = false
  ElMessage.success(`批量导入成功！已将 ${count} 位在册党员批量录入花名册。`)
}

// ==========================================
// 组织生活“三会一课”与主题党日业务逻辑 (支持单选/多选及彩色议题标签筛选)
// ==========================================
const meetingsList = ref([...MOCK_MEETING_RECORDS])
const meetingKeyword = ref('')
const meetingBranchFilter = ref([]) // 数组：支持单选/多选所属支部
const meetingTypeFilter = ref([]) // 数组：支持单选/多选组织生活类型
const meetingTagFilter = ref([]) // 数组：支持通过议题彩色标签单选/多选筛选

// 议题分类彩色标签库 (支持管理员新增，带颜色标记)
const availableTopicTags = ref([...AVAILABLE_TOPIC_TAGS])

const filteredMeetingsList = computed(() => {
  return meetingsList.value.filter(m => {
    // 支部筛选 (单选/多选兼容)
    if (meetingBranchFilter.value && meetingBranchFilter.value.length > 0) {
      if (!meetingBranchFilter.value.includes(m.branchName)) return false
    }
    // 组织生活类型筛选 (单选/多选兼容)
    if (meetingTypeFilter.value && meetingTypeFilter.value.length > 0) {
      if (!meetingTypeFilter.value.includes(m.meetingType)) return false
    }
    // 议题分类标签筛选 (单选/多选兼容)
    if (meetingTagFilter.value && meetingTagFilter.value.length > 0) {
      const rowTags = m.tags || (m.agendaItems ? m.agendaItems.map(a => a.tagName) : [])
      const matched = meetingTagFilter.value.some(selTag => rowTags.includes(selTag))
      if (!matched) return false
    }
    // 关键词综合搜索
    if (meetingKeyword.value) {
      const kw = meetingKeyword.value.toLowerCase()
      const matchTitle = m.title.toLowerCase().includes(kw)
      const matchMod = m.moderator.toLowerCase().includes(kw)
      const matchContent = m.content.toLowerCase().includes(kw)
      const matchAgendas = m.agendaItems ? m.agendaItems.some(a => a.topic.toLowerCase().includes(kw)) : false
      if (!matchTitle && !matchMod && !matchContent && !matchAgendas) return false
    }
    return true
  })
})

function getMeetingTypeTag(type) {
  const map = { 1: 'danger', 2: 'warning', 3: 'primary', 4: 'success' }
  return map[type] || 'info'
}

function resetMeetingFilters() {
  meetingKeyword.value = ''
  meetingBranchFilter.value = []
  meetingTypeFilter.value = []
  meetingTagFilter.value = []
}

function downloadMeetingDoc(row) {
  ElMessage.success(`已生成并下载《${row.docName}》（包含完整出勤签到表与会议纪要原件）`)
}

function exportMeetingsExcel() {
  ElMessage.success('已导出全集团 2025 年度“三会一课”与主题党日规范化开展台账 (.xlsx)')
}

// 管理议题分类标签
const manageTagsDialogVisible = ref(false)
const newTagName = ref('')
const newTagColor = ref('#c21c1d')

function openManageTagsDialog() {
  newTagName.value = ''
  newTagColor.value = '#c21c1d'
  manageTagsDialogVisible.value = true
}

function submitAddNewTag() {
  if (!newTagName.value.trim()) {
    ElMessage.warning('请输入标签名称！')
    return
  }
  const tagId = 'TAG_' + Date.now()
  availableTopicTags.value.push({
    id: tagId,
    name: newTagName.value.trim(),
    color: newTagColor.value,
    desc: '管理员新增自定义议题标签'
  })
  ElMessage.success(`成功新增议题分类标签【${newTagName.value.trim()}】！`)
  newTagName.value = ''
}

function deleteTopicTag(tagId) {
  availableTopicTags.value = availableTopicTags.value.filter(t => t.id !== tagId)
  ElMessage.success('已删除该标签')
}

// 记录与修改组织生活弹窗逻辑
const addMeetingDialogVisible = ref(false)
const isEditingMeeting = ref(false)
const editingMeetingId = ref(null)
const newMeetingForm = ref({
  branchName: '中共红河红数信息技术服务有限公司支部委员会',
  meetingType: 1,
  title: '',
  date: new Date().toISOString().slice(0, 10),
  place: '党员活动室',
  moderator: '李卫民 (支部书记)',
  speaker: '',
  expectedCount: 16,
  actualCount: 16,
  agendaItems: [
    { topic: '', tagId: 'TAG_DEV', tagName: '发展党员', tagColor: '#c21c1d' }
  ],
  content: '',
  relatedStep: 0,
  relatedMember: ''
})

function openAddMeetingDialog() {
  isEditingMeeting.value = false
  editingMeetingId.value = null
  newMeetingForm.value = {
    branchName: '中共红河红数信息技术服务有限公司支部委员会',
    meetingType: 1,
    title: '',
    date: new Date().toISOString().slice(0, 10),
    place: '党员活动室',
    moderator: '李卫民 (支部书记)',
    speaker: '',
    expectedCount: 16,
    actualCount: 16,
    agendaItems: [
      { topic: '', tagId: 'TAG_DEV', tagName: '发展党员', tagColor: '#c21c1d' }
    ],
    content: '',
    relatedStep: 0,
    relatedMember: ''
  }
  addMeetingDialogVisible.value = true
}

function openEditMeetingDialog(row) {
  isEditingMeeting.value = true
  editingMeetingId.value = row.id
  newMeetingForm.value = {
    branchName: row.branchName,
    meetingType: row.meetingType,
    title: row.title,
    date: row.date,
    place: row.place,
    moderator: row.moderator,
    speaker: row.speaker || '',
    expectedCount: row.expectedCount,
    actualCount: row.actualCount,
    agendaItems: row.agendaItems ? JSON.parse(JSON.stringify(row.agendaItems)) : [],
    content: row.content || '',
    relatedStep: row.relatedStep || 0,
    relatedMember: row.relatedMember || ''
  }
  addMeetingDialogVisible.value = true
}

function addAgendaItemRow() {
  newMeetingForm.value.agendaItems.push({
    topic: '',
    tagId: 'TAG_DEV',
    tagName: '发展党员',
    tagColor: '#c21c1d'
  })
}

function removeAgendaItemRow(idx) {
  newMeetingForm.value.agendaItems.splice(idx, 1)
}

function handleAgendaTagChange(agenda, tagId) {
  const tag = availableTopicTags.value.find(t => t.id === tagId)
  if (tag) {
    agenda.tagId = tag.id
    agenda.tagName = tag.name
    agenda.tagColor = tag.color
  }
}

function submitAddMeeting() {
  if (!newMeetingForm.value.title.trim()) {
    ElMessage.warning('请输入会议主要议题！')
    return
  }
  const typeMap = { 1: '支委会', 2: '支部党员大会', 3: '专题党课', 4: '主题党日' }
  const shortMap = {
    '中共红河红数信息技术服务有限公司支部委员会': '红数信息',
    '中共云南幂次科技有限公司支部委员会': '幂次科技',
    '中共红河链达科技有限公司支部委员会': '链达科技',
    '中共红河数据产业集团有限公司总支部委员会': '集团党总支'
  }
  const validAgendas = newMeetingForm.value.agendaItems.filter(a => a.topic && a.topic.trim())

  if (isEditingMeeting.value) {
    // 管理员修改已有会议记录
    const idx = meetingsList.value.findIndex(m => m.id === editingMeetingId.value)
    if (idx !== -1) {
      const item = meetingsList.value[idx]
      item.branchName = newMeetingForm.value.branchName
      item.branchShort = shortMap[newMeetingForm.value.branchName] || '党组织'
      item.meetingType = newMeetingForm.value.meetingType
      item.meetingTypeName = typeMap[newMeetingForm.value.meetingType] || '组织生活'
      item.title = newMeetingForm.value.title.trim()
      item.date = newMeetingForm.value.date
      item.place = newMeetingForm.value.place || '党员活动室'
      item.moderator = newMeetingForm.value.moderator || '支部书记'
      item.speaker = newMeetingForm.value.speaker || ''
      item.expectedCount = newMeetingForm.value.expectedCount
      item.actualCount = newMeetingForm.value.actualCount
      item.attendanceRate = Math.round(newMeetingForm.value.actualCount / newMeetingForm.value.expectedCount * 100)
      item.agendaItems = validAgendas
      item.tags = validAgendas.map(a => a.tagName)
      item.content = newMeetingForm.value.content || ''
      item.relatedStep = newMeetingForm.value.relatedStep || 0
      item.relatedMember = newMeetingForm.value.relatedMember || ''
    }
    addMeetingDialogVisible.value = false
    ElMessage.success('会议记录与决议纪实已成功修改更新！')
  } else {
    // 新增录入
    meetingsList.value.unshift({
      id: Date.now(),
      branchName: newMeetingForm.value.branchName,
      branchShort: shortMap[newMeetingForm.value.branchName] || '党组织',
      meetingType: newMeetingForm.value.meetingType,
      meetingTypeName: typeMap[newMeetingForm.value.meetingType] || '组织生活',
      title: newMeetingForm.value.title.trim(),
      date: newMeetingForm.value.date,
      place: newMeetingForm.value.place || '党员活动室',
      moderator: newMeetingForm.value.moderator || '支部书记',
      speaker: newMeetingForm.value.speaker || '',
      expectedCount: newMeetingForm.value.expectedCount,
      actualCount: newMeetingForm.value.actualCount,
      attendanceRate: Math.round(newMeetingForm.value.actualCount / newMeetingForm.value.expectedCount * 100),
      attendees: '支部在册党员及参会骨干',
      agendaItems: validAgendas,
      tags: validAgendas.map(a => a.tagName),
      content: newMeetingForm.value.content || '按规程召开会议，形成会议决议并归档纪要。',
      docName: `${shortMap[newMeetingForm.value.branchName]}_${newMeetingForm.value.date}_会议纪要.docx`,
      relatedStep: newMeetingForm.value.relatedStep || 0,
      relatedMember: newMeetingForm.value.relatedMember || ''
    })
    addMeetingDialogVisible.value = false
    ElMessage.success('组织生活会议纪要已归档入库！已同步更新三会一课台账与大屏指标。')
  }
}

// 会议全套规范附件管理逻辑 (通知、纪要、决议、签到表、现场照片)
const meetingAttachmentsDialogVisible = ref(false)
const currentMeetingForAttach = ref(null)
const uploadMeetingAttachType = ref('MINUTES')
const tempMeetingAttachFile = ref(null)
const currentMeetingAttachmentsList = ref([])

function openMeetingAttachmentsDialog(row) {
  currentMeetingForAttach.value = row
  uploadMeetingAttachType.value = 'MINUTES'
  tempMeetingAttachFile.value = null
  
  currentMeetingAttachmentsList.value = [
    {
      id: 1,
      attachType: 'NOTICE',
      attachTypeName: '会议通知',
      fileName: `${row.branchShort}_关于召开${row.title}的通知.pdf`,
      fileSize: '156 KB',
      uploadTime: `${row.date} 09:30`
    },
    {
      id: 2,
      attachType: 'MINUTES',
      attachTypeName: '会议纪要',
      fileName: row.docName,
      fileSize: '342 KB',
      uploadTime: `${row.date} 16:45`
    },
    {
      id: 3,
      attachType: 'SIGNIN',
      attachTypeName: '签到考勤表',
      fileName: `${row.branchShort}_${row.date}_到会党员签名表扫描件.pdf`,
      fileSize: '820 KB',
      uploadTime: `${row.date} 17:00`
    }
  ]
  if (row.relatedStep > 0) {
    currentMeetingAttachmentsList.value.push({
      id: 4,
      attachType: 'RESOLUTION',
      attachTypeName: '表决决议书',
      fileName: `${row.branchShort}_关于${row.relatedMember}同志的支部表决决议书.docx`,
      fileSize: '210 KB',
      uploadTime: `${row.date} 17:15`
    })
  }

  meetingAttachmentsDialogVisible.value = true
}

function getAttachCategoryTag(type) {
  const map = { NOTICE: 'info', MINUTES: 'primary', RESOLUTION: 'danger', SIGNIN: 'warning', PHOTO: 'success' }
  return map[type] || 'info'
}

function handleMeetingAttachSelected(file) {
  tempMeetingAttachFile.value = file.name
}

function confirmUploadMeetingAttach() {
  if (!tempMeetingAttachFile.value) {
    ElMessage.warning('请先选择要上传的本地附件文件！')
    return
  }
  const typeMap = { NOTICE: '会议通知', MINUTES: '会议纪要', RESOLUTION: '表决决议书', SIGNIN: '签到考勤表', PHOTO: '现场纪实照片' }
  currentMeetingAttachmentsList.value.unshift({
    id: Date.now(),
    attachType: uploadMeetingAttachType.value,
    attachTypeName: typeMap[uploadMeetingAttachType.value] || '附件材料',
    fileName: tempMeetingAttachFile.value,
    fileSize: '280 KB',
    uploadTime: new Date().toISOString().slice(0, 16).replace('T', ' ')
  })
  tempMeetingAttachFile.value = null
  ElMessage.success(`成功上传并归档《${typeMap[uploadMeetingAttachType.value]}》文件！`)
}

function downloadSingleMeetingAttach(row) {
  ElMessage.success(`正在下载《${row.fileName}》`)
}

function deleteMeetingAttach(row) {
  currentMeetingAttachmentsList.value = currentMeetingAttachmentsList.value.filter(a => a.id !== row.id)
  ElMessage.success(`已删除附件《${row.fileName}》`)
}

// ==========================================
// 3. 25步文书模板管理（双轨制：默认+导入）
// ==========================================
const templatesList = ref([...TEMPLATES_CATALOG_25])
const uploadDialogVisible = ref(false)
const selectedTemplateForUpload = ref(null)
const tempUploadedFileName = ref('')

const customizedTemplatesCount = computed(() => {
  return templatesList.value.filter(t => t.isCustomized).length
})

function downloadCurrentTemplate(tpl) {
  const targetName = tpl.isCustomized ? tpl.customName : tpl.defaultName
  ElMessage.success(`正在下载当前使用版本《${targetName}》`)
}

function downloadDefaultTemplate(tpl) {
  ElMessage.success(`正在下载系统官方标准模板《${tpl.defaultName}》`)
}

function openUploadDialog(tpl) {
  selectedTemplateForUpload.value = tpl
  tempUploadedFileName.value = ''
  uploadDialogVisible.value = true
}

function handleFileSelected(file) {
  tempUploadedFileName.value = file.name
}

function confirmUploadCustomTemplate() {
  if (!tempUploadedFileName.value) {
    ElMessage.warning('请先选择要上传的 .docx 格式模板文件')
    return
  }
  const tpl = selectedTemplateForUpload.value
  tpl.isCustomized = true
  tpl.customName = tempUploadedFileName.value
  tpl.version = 'v' + Math.floor(Math.random() * 9 + 1) + '.0 (企业定制)'
  tpl.updateUser = '当前管理员'
  tpl.updateTime = new Date().toISOString().slice(0, 10)
  
  uploadDialogVisible.value = false
  ElMessage.success(`第 ${tpl.stepId} 步自定义模板导入成功！已生效为全集团套打首选版本。`)
}

function restoreDefaultTemplate(tpl) {
  ElMessageBox.confirm(
    `确定要将第 ${tpl.stepId} 步《${tpl.name}》恢复为系统官方内置标准模板吗？`,
    '恢复默认确认',
    { confirmButtonText: '确认恢复', cancelButtonText: '取消', type: 'warning' }
  ).then(() => {
    tpl.isCustomized = false
    tpl.version = 'v1.0 (系统内置)'
    tpl.customName = ''
    tpl.updateUser = '系统内置'
    tpl.updateTime = '2024-01-01'
    ElMessage.success(`第 ${tpl.stepId} 步已恢复为官方标准版本！`)
  }).catch(() => {})
}

// ==========================================
// 组织与个人奖惩/荣誉台账业务逻辑
// ==========================================
const honorsList = ref([...MOCK_HONOR_PUNISHMENT_LIST])
const honorKeyword = ref('')
const honorCategoryFilter = ref(null)
const honorRecordTypeFilter = ref(null)
const honorBranchFilter = ref([])
const honorLevelFilter = ref('')

const filteredHonorsList = computed(() => {
  return honorsList.value.filter(item => {
    if (honorCategoryFilter.value !== null && item.category !== honorCategoryFilter.value) return false
    if (honorRecordTypeFilter.value !== null && item.recordType !== honorRecordTypeFilter.value) return false
    if (honorBranchFilter.value && honorBranchFilter.value.length > 0) {
      if (!honorBranchFilter.value.includes(item.orgName)) return false
    }
    if (honorLevelFilter.value && item.level !== honorLevelFilter.value) return false
    if (honorKeyword.value) {
      const kw = honorKeyword.value.toLowerCase()
      const matchTitle = item.title.toLowerCase().includes(kw)
      const matchTarget = item.targetName.toLowerCase().includes(kw)
      const matchDoc = item.docNo ? item.docNo.toLowerCase().includes(kw) : false
      const matchReason = item.reasonContent ? item.reasonContent.toLowerCase().includes(kw) : false
      if (!matchTitle && !matchTarget && !matchDoc && !matchReason) return false
    }
    return true
  })
})

function getHonorCount(category, recordType) {
  return honorsList.value.filter(item => {
    if (category !== null && item.category !== category) return false
    if (recordType !== null && item.recordType !== recordType) return false
    return true
  }).length
}

function getHonorLevelTag(level) {
  const map = { '国家级': 'danger', '省部级': 'warning', '州级/市级': 'primary', '集团级': 'success', '支部级': 'info' }
  return map[level] || 'info'
}

function resetHonorFilters() {
  honorKeyword.value = ''
  honorCategoryFilter.value = null
  honorRecordTypeFilter.value = null
  honorBranchFilter.value = []
  honorLevelFilter.value = ''
}

function exportHonorsExcel() {
  ElMessage.success(`已导出全集团党组织及党员奖惩/荣誉台账清单 (共 ${honorsList.value.length} 项，.xlsx 格式)`)
}

// 登记与修改奖惩荣誉弹窗
const honorDialogVisible = ref(false)
const isEditingHonor = ref(false)
const editingHonorId = ref(null)
const newHonorForm = ref({
  category: 2,
  recordType: 1,
  targetName: '中共云南幂次科技有限公司支部委员会',
  orgName: '中共云南幂次科技有限公司支部委员会',
  title: '',
  level: '省部级',
  grantOrg: '',
  docNo: '',
  recordDate: new Date().toISOString().slice(0, 10),
  reasonContent: ''
})

function openAddHonorDialog() {
  isEditingHonor.value = false
  editingHonorId.value = null
  newHonorForm.value = {
    category: 2,
    recordType: 1,
    targetName: '中共云南幂次科技有限公司支部委员会',
    orgName: '中共云南幂次科技有限公司支部委员会',
    title: '',
    level: '集团级',
    grantOrg: '中共红河数据产业集团有限公司总支部委员会',
    docNo: '红数党总发〔2025〕08号',
    recordDate: new Date().toISOString().slice(0, 10),
    reasonContent: ''
  }
  honorDialogVisible.value = true
}

function openEditHonorDialog(row) {
  isEditingHonor.value = true
  editingHonorId.value = row.id
  newHonorForm.value = {
    category: row.category,
    recordType: row.recordType,
    targetName: row.targetName,
    orgName: row.orgName,
    title: row.title,
    level: row.level,
    grantOrg: row.grantOrg || '',
    docNo: row.docNo || '',
    recordDate: row.recordDate,
    reasonContent: row.reasonContent || ''
  }
  honorDialogVisible.value = true
}

function handleHonorCategoryChange(val) {
  if (val === 2) {
    newHonorForm.value.targetName = '中共云南幂次科技有限公司支部委员会'
    newHonorForm.value.orgName = '中共云南幂次科技有限公司支部委员会'
  } else {
    newHonorForm.value.targetName = '朱文华'
    newHonorForm.value.orgName = '中共红河数据产业集团有限公司总支部委员会'
  }
}

function handleOrgTargetChange(val) {
  newHonorForm.value.orgName = val
}

function handleMemberTargetChange(val) {
  const found = rosterList.value.find(m => m.name === val)
  if (found) {
    newHonorForm.value.orgName = found.branchName
  }
}

function submitSaveHonor() {
  if (!newHonorForm.value.title.trim()) {
    ElMessage.warning('请输入奖惩/表彰名称！')
    return
  }
  if (!newHonorForm.value.targetName) {
    ElMessage.warning('请选择或输入获奖/受处分主体！')
    return
  }

  const shortMap = {
    '中共红河红数信息技术服务有限公司支部委员会': '红数信息',
    '中共云南幂次科技有限公司支部委员会': '幂次科技',
    '中共红河链达科技有限公司支部委员会': '链达科技',
    '中共红河数据产业集团有限公司总支部委员会': '集团党总支'
  }

  if (isEditingHonor.value) {
    const idx = honorsList.value.findIndex(h => h.id === editingHonorId.value)
    if (idx !== -1) {
      const item = honorsList.value[idx]
      item.category = newHonorForm.value.category
      item.categoryName = newHonorForm.value.category === 2 ? '组织荣誉/奖惩' : '个人荣誉/奖惩'
      item.recordType = newHonorForm.value.recordType
      item.recordTypeName = newHonorForm.value.recordType === 1 ? '荣誉表彰' : '纪律处分/诫勉'
      item.title = newHonorForm.value.title.trim()
      item.targetName = newHonorForm.value.targetName
      item.orgName = newHonorForm.value.orgName
      item.orgShort = shortMap[newHonorForm.value.orgName] || '党组织'
      item.level = newHonorForm.value.level
      item.grantOrg = newHonorForm.value.grantOrg
      item.docNo = newHonorForm.value.docNo
      item.recordDate = newHonorForm.value.recordDate
      item.reasonContent = newHonorForm.value.reasonContent
    }
    honorDialogVisible.value = false
    ElMessage.success('奖惩/荣誉记录已成功修改更新！')
  } else {
    honorsList.value.unshift({
      id: Date.now(),
      category: newHonorForm.value.category,
      categoryName: newHonorForm.value.category === 2 ? '组织荣誉/奖惩' : '个人荣誉/奖惩',
      recordType: newHonorForm.value.recordType,
      recordTypeName: newHonorForm.value.recordType === 1 ? '荣誉表彰' : '纪律处分/诫勉',
      title: newHonorForm.value.title.trim(),
      targetName: newHonorForm.value.targetName,
      orgName: newHonorForm.value.orgName,
      orgShort: shortMap[newHonorForm.value.orgName] || '党组织',
      level: newHonorForm.value.level,
      grantOrg: newHonorForm.value.grantOrg,
      docNo: newHonorForm.value.docNo,
      recordDate: newHonorForm.value.recordDate,
      reasonContent: newHonorForm.value.reasonContent,
      attachmentPath: '规范表彰证明文档.pdf'
    })
    honorDialogVisible.value = false
    ElMessage.success('奖惩/荣誉记录已成功登记入库！')
  }
}

function deleteHonorItem(row) {
  ElMessageBox.confirm(
    `确定要删除《${row.title}》（主体：${row.targetName}）的记录吗？`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(() => {
    honorsList.value = honorsList.value.filter(h => h.id !== row.id)
    ElMessage.success('记录已成功删除')
  }).catch(() => {})
}

// ==========================================
// 4. 一人一档抽屉与合规防错引擎交互
// ==========================================
const drawerVisible = ref(false)
const currentMember = ref(null)
const selectedStepInDrawer = ref(1)
const previewDialogVisible = ref(false)
const previewDocTitle = ref('材料详情')

const all25StepsFlat = computed(() => {
  const list = []
  STAGES_AND_STEPS.forEach(st => {
    st.steps.forEach(sp => {
      list.push(sp)
    })
  })
  return list
})

const activeDrawerStepInfo = computed(() => {
  return all25StepsFlat.value.find(s => s.stepId === selectedStepInDrawer.value) || all25StepsFlat.value[0]
})

const stepMaterialsList = computed(() => {
  if (!currentMember.value) return []
  return activeDrawerStepInfo.value.docs.map((docName, idx) => {
    return {
      id: idx + 1,
      name: docName,
      status: selectedStepInDrawer.value <= currentMember.value.currentStepId ? 'approved' : 'pending',
      time: selectedStepInDrawer.value < currentMember.value.currentStepId ? '已归档' : (selectedStepInDrawer.value === currentMember.value.currentStepId ? '2025-03-18' : '待流转办理')
    }
  })
})

const activeDrawerStepAuditClass = computed(() => {
  if (!currentMember.value) return 'audit-normal'
  if (currentMember.value.id === 101 && selectedStepInDrawer.value >= 9) return 'audit-danger'
  if (currentMember.value.id === 105 && selectedStepInDrawer.value === 2) return 'audit-warning'
  return 'audit-pass'
})

const activeDrawerStepAuditDesc = computed(() => {
  if (!currentMember.value) return ''
  if (currentMember.value.id === 101 && selectedStepInDrawer.value >= 9) {
    return '【系统强阻断】检测到该成员确定为入党积极分子仅 290 天（未满 365 天）。依据《细则》第十三条，考察期不足一年不得列为发展对象，系统阻断推进！'
  }
  if (currentMember.value.id === 105 && selectedStepInDrawer.value === 2) {
    return '【临期预警】入党申请书递交已过 22 天，距离“1个月内必须完成支部谈话”红线仅剩 8 天，请尽快组织谈话并归档《谈话记录表》。'
  }
  return '【合规审计通过】该步骤前置环节时间顺序合法、材料齐备，符合《中国共产党发展党员工作细则》要求。'
})

function getStepName(stepId) {
  const step = all25StepsFlat.value.find(s => s.stepId === stepId)
  return step ? step.name : ''
}

function getStageTagType(stageId) {
  const map = { 1: 'info', 2: 'warning', 3: 'primary', 4: 'danger', 5: 'success' }
  return map[stageId] || 'info'
}

function getComplianceShortText(alert) {
  if (!alert) return '合规正常'
  if (alert.type === 'danger') return '考察不足1年(锁死)'
  if (alert.type === 'warning') return '临期预警待办'
  return '合规流转中'
}

function getMaterialTagType(status) {
  const map = { approved: 'success', pending: 'warning', waiting_upload: 'danger' }
  return map[status] || 'info'
}

function getMaterialStatusText(status) {
  const map = { approved: '已审核通过', pending: '待组织审核', waiting_upload: '待上传提交' }
  return map[status] || '未开始'
}

function openMemberDrawer(member) {
  currentMember.value = member
  selectedStepInDrawer.value = member.currentStepId
  drawerVisible.value = true
}

function quickProgressStep(member) {
  openMemberDrawer(member)
}

function handleAdvanceStep() {
  if (currentMember.value.id === 101 && currentMember.value.currentStepId === 7) {
    ElMessageBox.alert(
      '【系统硬阻断】张强同志作为入党积极分子考察期仅 290 天（未满法定 365 天）。依据《中国共产党发展党员工作细则》第十三条，严禁在考察期不足一年时提前确定为发展对象！如遇巡视巡察将判定为违规入党。',
      '党务合规审计阻断',
      { confirmButtonText: '已知晓并严格按规章执行', type: 'error' }
    )
    return
  }
  ElMessage.success({ message: `第 ${selectedStepInDrawer.value} 步审核归档通过！已成功推进至下一业务节点。`, duration: 3000 })
}

function previewMaterial(row) {
  previewDocTitle.value = row.name
  previewDialogVisible.value = true
}

function downloadSingleDoc(row) {
  ElMessage.success(`已生成 ${row.name}（已自动填充 ${currentMember.value.name} 组织信息与编号），已下载！`)
}

function mockBatchExportDocs() {
  ElMessage.success(`已一键打包生成【${currentMember.value.name}】全流程档案（包含 20+ 份规范 Word/PDF），ZIP 压缩包已导出！`)
}

function downloadDocSuccess() {
  ElMessage.success(`已成功导出 ${previewDocTitle.value} Word 格式文档！`)
  previewDialogVisible.value = false
}

function openAddDialog() {
  ElMessage.info('新建入党申请人功能已就绪，请输入员工工号关联企业人事库。')
}

function exportTableData() {
  ElMessage.success('已导出红河数据产业集团 2025 年度发展党员合规台账（Excel格式）。')
}
</script>

<style scoped>
.dj-root {
  min-height: 100vh;
}

/* ========================================================================= */
/* 普通业务端样式                                                            */
/* ========================================================================= */
.dj-app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.dj-header {
  height: 68px;
  background: linear-gradient(90deg, #990f10 0%, #c21c1d 60%, #a61214 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  box-shadow: 0 2px 10px rgba(153, 15, 16, 0.25);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo-badge {
  width: 42px;
  height: 42px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.title-group h1 {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.5px;
  margin: 0;
  color: #fff;
}

.sub-title {
  font-size: 12px;
  opacity: 0.88;
  color: #ffeaea;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.big-screen-btn {
  font-weight: bold;
  border: 1px solid #ffbe4f !important;
}

.role-switcher {
  display: flex;
  align-items: center;
  gap: 6px;
}

.role-label {
  font-size: 13px;
  color: #ffeaea;
}

.org-tag {
  background: rgba(0, 0, 0, 0.2) !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
}

/* 跑马灯合规警报条 */
.compliance-marquee {
  background: #fff8e6;
  border-bottom: 1px solid #f9e2ae;
  padding: 8px 28px;
  display: flex;
  align-items: center;
  gap: 20px;
  overflow: hidden;
}

.marquee-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #c21c1d;
  font-weight: bold;
  font-size: 13px;
  white-space: nowrap;
}

.marquee-content {
  display: flex;
  align-items: center;
  gap: 24px;
  font-size: 12.5px;
  overflow-x: auto;
  white-space: nowrap;
}

.alert-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.alert-danger span {
  color: #c21c1d;
  font-weight: 500;
}

.alert-warning span {
  color: #b88230;
}

.alert-info span {
  color: #409eff;
}

.dj-main-container {
  padding: 16px 28px 40px;
  flex: 1;
}

.dj-nav-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 600;
  height: 48px;
  line-height: 48px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 5大阶段横幅 */
.stages-overview-card {
  background: #fff;
  border-radius: 8px;
  padding: 18px 22px;
  margin-bottom: 18px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.card-header-row h3 {
  font-size: 15px;
  font-weight: 700;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
}

.rule-hint {
  font-size: 12px;
  color: #909399;
}

.stages-stepper {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.stage-step-item {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 12px 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;
}

.stage-step-item:hover {
  border-color: #c21c1d;
  background: #fff5f5;
  transform: translateY(-2px);
}

.stage-step-item.active-stage {
  border-color: #c21c1d;
  background: #fef0f0;
  box-shadow: 0 0 0 1px #c21c1d inset;
}

.stage-index-num {
  font-size: 20px;
  font-weight: 800;
  color: #c21c1d;
  opacity: 0.85;
}

.stage-info {
  flex: 1;
}

.stage-title {
  font-size: 13px;
  font-weight: bold;
  color: #2d3748;
  margin-bottom: 4px;
}

.stage-meta {
  font-size: 11.5px;
  color: #718096;
  display: flex;
  align-items: center;
  gap: 8px;
}

.stage-arrow {
  color: #cbd5e1;
}

/* 列表卡片与工具栏 */
.members-table-card, .roster-container-card, .templates-mgr-container {
  background: #fff;
  border-radius: 8px;
  padding: 18px 22px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 花名册统计横幅 */
.roster-stats-banner {
  background: #fdf6ec;
  border: 1px solid #faecd8;
  border-radius: 8px;
  padding: 14px 20px;
  display: flex;
  align-items: center;
  justify-content: space-around;
  margin-bottom: 18px;
}

.stat-pill {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.pill-label {
  font-size: 12px;
  color: #606266;
}

.pill-val {
  font-size: 20px;
  font-weight: 800;
  color: #303133;
}

.color-red { color: #c21c1d !important; }
.color-orange { color: #e6a23c !important; }
.color-blue { color: #409eff !important; }
.color-green { color: #67c23a !important; }
.color-gold { color: #f4d03f !important; }
.color-cyan { color: #00e5ff !important; }

.roster-name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sub-workno {
  font-size: 11px;
  color: #909399;
}

.standing-text {
  color: #c21c1d;
  font-size: 13.5px;
}

.date-col {
  display: flex;
  flex-direction: column;
  font-size: 11.5px;
}

.code-font {
  font-family: monospace;
  font-size: 11.5px;
  color: #606266;
}

/* 模板管理页 */
.mgr-header-card {
  background: #fdf2f2;
  border: 1px solid #fcdada;
  border-radius: 8px;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.mgr-header-left h3 {
  margin: 0 0 6px 0;
  font-size: 16px;
  color: #c21c1d;
  display: flex;
  align-items: center;
  gap: 6px;
}

.mgr-header-left p {
  margin: 0;
  font-size: 12.5px;
  color: #606266;
}

.mgr-header-right {
  display: flex;
  gap: 10px;
}

.tpl-name-cell {
  display: flex;
  flex-direction: column;
}

.tpl-code-text {
  font-size: 11px;
  color: #909399;
}

.tpl-status-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.version-tag {
  font-size: 11px;
  color: #909399;
}

.tpl-file-meta {
  display: flex;
  flex-direction: column;
  font-size: 12px;
}

.custom-file-name {
  color: #b88230;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.default-file-name {
  color: #606266;
  display: flex;
  align-items: center;
  gap: 4px;
}

.update-sub {
  font-size: 11px;
  color: #a0aec0;
}

/* 驾驶舱与工作台通用卡片 */
.member-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-red {
  background-color: #c21c1d !important;
  color: #fff;
  font-weight: bold;
}

.avatar-red-big {
  background-color: #c21c1d !important;
  color: #fff;
  font-size: 20px;
  font-weight: bold;
}

.member-meta {
  display: flex;
  flex-direction: column;
}

.name-line {
  display: flex;
  align-items: center;
  gap: 6px;
}

.sub-job {
  font-size: 12px;
  color: #909399;
}

.branch-cell {
  display: flex;
  flex-direction: column;
}

.branch-title {
  font-weight: 600;
  font-size: 13px;
  color: #303133;
}

.dept-title {
  font-size: 11.5px;
  color: #909399;
}

.tags-wrapper {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.step-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.step-name {
  font-size: 12.5px;
  color: #2c3e50;
}

.stay-days {
  font-size: 11px;
  color: #909399;
}

.compliance-cell {
  display: flex;
  align-items: center;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* 驾驶舱样式 */
.cockpit-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.metric-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.metric-card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.metric-title {
  font-size: 13.5px;
  font-weight: 600;
  color: #4a5568;
}

.metric-numbers {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 10px;
}

.actual-val {
  font-size: 26px;
  font-weight: 800;
  color: #c21c1d;
}

.target-val {
  font-size: 11.5px;
  color: #718096;
}

.metric-desc {
  font-size: 11.5px;
  color: #718096;
  margin-top: 8px;
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.chart-box {
  background: #fff;
  border-radius: 8px;
  padding: 18px 22px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f2f5;
}

.chart-header h4 {
  margin: 0;
  font-size: 14.5px;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #303133;
}

.plan-stat {
  font-size: 12px;
  color: #c21c1d;
  font-weight: 600;
}

.funnel-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px 0;
}

.funnel-stage {
  height: 42px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
}

.stage-1 { background: #909399; width: 100%; }
.stage-2 { background: #e6a23c; width: 85%; margin: 0 auto; }
.stage-3 { background: #409eff; width: 70%; margin: 0 auto; }
.stage-4 { background: #e74c3c; width: 55%; margin: 0 auto; }
.stage-5 { background: #c21c1d; width: 42%; margin: 0 auto; }

.branch-progress-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.branch-item .b-info {
  display: flex;
  justify-content: space-between;
  font-size: 12.5px;
  color: #4a5568;
  margin-bottom: 6px;
}

.dual-cultivate-card {
  background: #fff;
  border-radius: 8px;
  padding: 18px 22px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.dual-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.dual-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
}

.dual-stat-box {
  background: #fdf6ec;
  border: 1px solid #faecd8;
  border-radius: 6px;
  padding: 16px;
  text-align: center;
}

.dual-stat-box .num-text {
  font-size: 24px;
  font-weight: 800;
  color: #b88230;
  margin-bottom: 4px;
}

.dual-stat-box .sub-text {
  font-size: 12px;
  color: #606266;
}

/* 抽屉样式 */
.drawer-inner-layout {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-bottom: 20px;
}

.member-header-card {
  background: #fdf2f2;
  border: 1px solid #fcdada;
  border-radius: 8px;
  padding: 18px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-left {
  display: flex;
  align-items: center;
  gap: 18px;
}

.main-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.main-info h2 {
  margin: 0;
  font-size: 20px;
  color: #2c3e50;
}

.dept-info {
  display: flex;
  gap: 16px;
  font-size: 12.5px;
  color: #606266;
}

.dept-info span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.profile-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.cultivator-box {
  font-size: 12.5px;
  color: #606266;
}

.drawer-compliance-alert {
  margin: 0;
}

.drawer-split-body {
  display: grid;
  grid-template-columns: 290px 1fr;
  gap: 20px;
  min-height: 520px;
}

.timeline-sidebar {
  border-right: 1px solid #e4e7ed;
  padding-right: 14px;
  max-height: 600px;
  overflow-y: auto;
}

.sidebar-header {
  margin-bottom: 12px;
}

.sidebar-header h4 {
  font-size: 14px;
  margin: 0 0 2px 0;
  color: #303133;
}

.sidebar-header .hint {
  font-size: 11px;
  color: #909399;
}

.steps-timeline-list {
  display: flex;
  flex-direction: column;
}

.timeline-node {
  display: flex;
  gap: 10px;
  cursor: pointer;
  padding: 6px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}

.timeline-node:hover {
  background: #f5f7fa;
}

.timeline-node.node-selected {
  background: #fef0f0;
}

.node-icon-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 24px;
}

.icon-circle {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: bold;
}

.icon-circle.done {
  background: #67c23a;
  color: #fff;
}

.icon-circle.current {
  background: #c21c1d;
  color: #fff;
  box-shadow: 0 0 0 3px rgba(194, 28, 29, 0.25);
  animation: pulse 1.8s infinite;
}

.icon-circle.future {
  background: #e4e7ed;
  color: #909399;
}

.line-segment {
  width: 2px;
  flex: 1;
  background: #e4e7ed;
  min-height: 24px;
  margin-top: 4px;
}

.node-info-col {
  flex: 1;
}

.node-title {
  font-size: 12.5px;
  font-weight: 600;
  color: #2c3e50;
}

.node-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
  margin-top: 2px;
}

.n-role {
  color: #909399;
}

.pulse-tag {
  color: #c21c1d;
  font-weight: bold;
}

.done-tag {
  color: #67c23a;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(194, 28, 29, 0.5); }
  70% { box-shadow: 0 0 0 6px rgba(194, 28, 29, 0); }
  100% { box-shadow: 0 0 0 0 rgba(194, 28, 29, 0); }
}

.step-detail-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.step-main-header {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 12px;
}

.header-tit {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.header-tit h3 {
  margin: 0;
  font-size: 17px;
  color: #2c3e50;
}

.rule-box {
  font-size: 12.5px;
  color: #606266;
  background: #f8fafc;
  padding: 8px 12px;
  border-radius: 4px;
}

.audit-radar-box {
  border-radius: 6px;
  padding: 12px 16px;
}

.audit-danger {
  background: #fef0f0;
  border: 1px solid #fde2e2;
  color: #c21c1d;
}

.audit-warning {
  background: #fdf6ec;
  border: 1px solid #faecd8;
  color: #b88230;
}

.audit-pass {
  background: #f0f9eb;
  border: 1px solid #e1f3d8;
  color: #67c23a;
}

.radar-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: bold;
  font-size: 13.5px;
  margin-bottom: 4px;
}

.radar-p {
  margin: 0;
  font-size: 12.5px;
}

.materials-section {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 14px;
}

.sec-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.sec-header h4 {
  margin: 0;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.step-footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 14px;
  border-top: 1px solid #ebeef5;
}

/* 预览弹窗纸质感 */
.doc-preview-modal-body {
  background: #eaeaea;
  padding: 20px;
  display: flex;
  justify-content: center;
}

.doc-paper {
  background: #fff;
  width: 100%;
  max-width: 520px;
  min-height: 380px;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.15);
  padding: 30px;
  font-family: "SimSun", "Songti SC", serif;
}

.doc-paper-header {
  text-align: center;
  border-bottom: 2px solid #c21c1d;
  padding-bottom: 12px;
  margin-bottom: 18px;
}

.doc-paper-header h3 {
  font-size: 18px;
  color: #c21c1d;
  letter-spacing: 1px;
  margin: 0 0 6px 0;
}

.doc-code {
  font-size: 11px;
  color: #909399;
}

.doc-paper-content p {
  font-size: 13.5px;
  line-height: 1.8;
  margin-bottom: 8px;
  color: #333;
}

.sign-seal-box {
  margin-top: 40px;
  text-align: right;
  font-size: 13px;
  position: relative;
}

.seal-mark {
  color: rgba(194, 28, 29, 0.5);
  font-weight: bold;
  font-size: 15px;
  margin-bottom: 4px;
}

.sign-line {
  margin-bottom: 4px;
}

.date-line {
  color: #666;
}
</style>
