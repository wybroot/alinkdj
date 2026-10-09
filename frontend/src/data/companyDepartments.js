// 集团实际行政部门。岗位为初始建议值，可在人员建档时修改。
export const COMPANY_DEPARTMENTS = [
  {
    orgId: 1,
    branchName: '中共红河数据产业集团有限公司总支部委员会',
    companyName: '红河数据产业集团有限公司',
    departments: [
      { name: '综合管理部', jobTitle: '综合管理专员' },
      { name: '财务管理部', jobTitle: '财务会计' },
      { name: '数据业务部', jobTitle: '数据业务专员' },
      { name: '投资经营部', jobTitle: '投资运营专员' },
      { name: '项目管理部', jobTitle: '项目经理' },
      { name: '风险管控部', jobTitle: '风险合规专员' }
    ]
  },
  {
    orgId: 2,
    branchName: '中共红河红数信息技术服务有限公司支部委员会',
    companyName: '红河红数信息技术服务有限公司',
    departments: [
      { name: '技术部', jobTitle: '运维工程师' },
      { name: '综合部', jobTitle: '行政人事专员' },
      { name: '市场部', jobTitle: '市场拓展专员' }
    ]
  },
  {
    orgId: 3,
    branchName: '中共云南幂次科技有限公司支部委员会',
    companyName: '云南幂次科技有限公司',
    departments: [
      { name: '商务合约部', jobTitle: '商务合约专员' },
      { name: '产品运营部', jobTitle: '产品运营专员' },
      { name: '技术创新部', jobTitle: '软件研发工程师' }
    ]
  },
  {
    orgId: 4,
    branchName: '中共红河链达科技有限公司支部委员会',
    companyName: '红河链达科技有限公司',
    departments: [
      { name: '综合部', jobTitle: '行政人事专员' },
      { name: '运营部', jobTitle: '运营主管' },
      { name: '供应链管理部', jobTitle: '供应链管理专员' }
    ]
  }
]

export function getCompanyByBranch(branchName) {
  return COMPANY_DEPARTMENTS.find(company => company.branchName === branchName)
}

export function getDepartmentOptions(branchName) {
  const company = getCompanyByBranch(branchName)
  return company ? company.departments.map(department => ({
    label: department.name,
    value: `${company.companyName} · ${department.name}`,
    jobTitle: department.jobTitle
  })) : []
}
