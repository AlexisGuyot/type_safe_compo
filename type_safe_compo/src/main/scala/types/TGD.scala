package types

import shapeless._
import scalaz._

import functions._

object tgd {
    import support_functions._ 

    abstract class DepartmentS { type DeptName <: Witness ; def deptName(implicit w: DeptName) = w.value.asInstanceOf[w.T] }
    sealed trait GroupS { type GroupID <: Witness ; type DeptName <: Witness ; def groupID(implicit w: GroupID) = w.value.asInstanceOf[w.T] ; def deptName(implicit w: DeptName) = w.value.asInstanceOf[w.T] }
    sealed trait BudgetS { type BudgetID <: Witness ; type DeptName <: Witness ; def budgetID(implicit w: BudgetID) = w.value.asInstanceOf[w.T] ; def deptName(implicit w: DeptName) = w.value.asInstanceOf[w.T] }

    sealed trait DepartmentT { type DeptName <: Witness ; type DeptID <: Witness ; def deptName(implicit w: DeptName) = w.value.asInstanceOf[w.T] ; def deptID(implicit w: DeptID) = w.value.asInstanceOf[w.T] }
    sealed trait GroupT { type GroupID <: Witness ; type DeptID <: Witness ; type Leader <: Witness ; def groupID(implicit w: GroupID) = w.value.asInstanceOf[w.T] ; def deptID(implicit w: DeptID) = w.value.asInstanceOf[w.T] ; def leader(implicit w: Leader) = w.value.asInstanceOf[w.T] }
    sealed trait BudgetT { type BudgetID <: Witness ; type DeptID <: Witness ; type Amount <: Witness ; def budgetID(implicit w: BudgetID) = w.value.asInstanceOf[w.T] ; def deptID(implicit w: DeptID) = w.value.asInstanceOf[w.T] ; def amount(implicit w: Amount) = w.value.asInstanceOf[w.T] }

    sealed trait Fd[DeptName <: Witness] extends DepFn1[DeptName] { type Out <: Witness }
    sealed trait Fl[DeptName <: Witness, GroupID <: Witness] extends DepFn2[DeptName, GroupID] { type Out <: Witness }
    sealed trait Fa[DeptName <: Witness, BudgetID <: Witness] extends DepFn2[DeptName, BudgetID] { type Out <: Witness }

    object Fd {
        type Aux[DeptName <: Witness, Out0 <: Witness] = Fd[DeptName] { type Out = Out0 }
        def apply[DeptName <: Witness](ok: Fd[DeptName]): Aux[DeptName, ok.Out] = ok
        implicit def fd[DeptName <: Witness]: Aux[DeptName, Witness.Aux[Witness.`0`.T]] = 
            new Fd[DeptName] { type Out = Witness.Aux[Witness.`0`.T] ; def apply(name: DeptName) = Witness(0) }
    }
    object Fl {
        type Aux[DeptName <: Witness, GroupID <: Witness, Out0 <: Witness] = Fl[DeptName, GroupID] { type Out = Out0 }
        def apply[DeptName <: Witness, GroupID <: Witness](ok: Fl[DeptName, GroupID]): Aux[DeptName, GroupID, ok.Out] = ok
        implicit def fl[DeptName <: Witness, GroupID <: Witness]: Aux[DeptName, GroupID, Witness.Aux[Witness.`"Jean-Pierre"`.T]] = 
        new Fl[DeptName, GroupID]  { type Out = Witness.Aux[Witness.`"Jean-Pierre"`.T] ; def apply(name: DeptName, group: GroupID) = Witness("Jean-Pierre") }
    }
    object Fa {
        type Aux[DeptName <: Witness, BudgetID <: Witness, Out0 <: Witness] = Fa[DeptName, BudgetID] { type Out = Out0 }
        def apply[DeptName <: Witness, BudgetID <: Witness](ok: Fa[DeptName, BudgetID]): Aux[DeptName, BudgetID, ok.Out] = ok
        implicit def fa[DeptName <: Witness, BudgetID <: Witness]: Aux[DeptName, BudgetID, Witness.Aux[Witness.`12345.6`.T]] = 
            new Fa[DeptName, BudgetID]  { type Out = Witness.Aux[Witness.`12345.6`.T] ; def apply(name: DeptName, budget: BudgetID) = Witness(12345.6) }
    }

    implicit def sigma1[FdOut <: Witness](
        ds: DepartmentS
    )(
        implicit
        fd: Fd.Aux[ds.DeptName, FdOut]
    ): DepartmentT { type DeptName = ds.DeptName ; type DeptID = FdOut } = new DepartmentT { type DeptName = ds.DeptName ; type DeptID = FdOut }

    implicit def sigma2[FdOut <: Witness, FlOut <: Witness](
        ds: DepartmentS, 
        gs: GroupS
    )(
        implicit
        fd: Fd.Aux[ds.DeptName, FdOut],
        fl: Fl.Aux[ds.DeptName, gs.GroupID, FlOut]
    ): GroupT { type GroupID = gs.GroupID ; type DeptID = FdOut ; type Leader = FlOut } = new GroupT { type GroupID = gs.GroupID ; type DeptID = FdOut ; type Leader = FlOut }

    implicit def sigma3[FdOut <: Witness, FaOut <: Witness](
        ds: DepartmentS, 
        bs: BudgetS
    )(
        implicit
        fd: Fd.Aux[ds.DeptName, FdOut],
        fa: Fa.Aux[ds.DeptName, bs.BudgetID, FaOut]
    ): BudgetT { type BudgetID = bs.BudgetID ; type DeptID = FdOut ; type Amount = FaOut } = new BudgetT { type BudgetID = bs.BudgetID ; type DeptID = FdOut ; type Amount = FaOut }

    def micro(depS: DepartmentS, grpS: GroupS, budgS: BudgetS) = (sigma1(depS), sigma2(depS, grpS), sigma3(depS, budgS))

    def printSchemaSource(depS: DepartmentS, grpS: GroupS, budgS: BudgetS)(
        implicit 
        deptName: depS.DeptName,
        groupID: grpS.GroupID,
        budgetID: budgS.BudgetID
    ) = println(s"""
        Department(DeptName = ${deptName.value})
        Group(GroupID = ${groupID.value}, DeptName = ${deptName.value})
        Budget(BudgetID = ${budgetID.value}, DeptName = ${deptName.value})
    """)

    def printSchemaTarget(depT: DepartmentT, grpT: GroupT, budgT: BudgetT)(
        implicit 
        deptName: depT.DeptName,
        deptID: depT.DeptID,
        leader: grpT.Leader,
        groupID: grpT.GroupID,
        budgetID: budgT.BudgetID,
        amount: budgT.Amount
    ) = println(s"""
        Department(DeptName = ${deptName.value}, DeptID = ${deptID.value})
        Group(GroupID = ${groupID.value}, DeptID = ${deptID.value}, Leader = ${leader.value})
        Budget(BudgetID = ${budgetID.value}, DeptID = ${deptID.value}, Amount = ${amount.value})
    """)

    def tests_tgds() = {
        val depVentes = new DepartmentS { type DeptName = Witness.Aux[Witness.`"ventes"`.T] }
        val grpVentes = new GroupS { type GroupID = Witness.Aux[Witness.`1`.T] ; type DeptName = Witness.Aux[Witness.`"ventes"`.T] }
        val budgVentes = new BudgetS { type BudgetID = Witness.Aux[Witness.`9`.T]  ; type DeptName = Witness.Aux[Witness.`"ventes"`.T] }

        val (depT, grpT, budgT) = micro(depVentes, grpVentes, budgVentes)

        printSchemaSource(depVentes, grpVentes, budgVentes)
        printSchemaTarget(depT, grpT, budgT)
    }
}