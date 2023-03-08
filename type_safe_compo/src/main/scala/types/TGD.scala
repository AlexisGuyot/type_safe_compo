package types

import shapeless._
import scalaz._

import functions._

object tgd {
    import support_functions._ 

    type Attribute = Witness    // Attributes in Schema Mappings are Represented with Singleton Types (Witness)


    // -------- Running Example: http://publications.rwth-aachen.de/record/774437/files/774437.pdf

    // -------- Source Schema

    trait DepartmentS { 
        type DeptName <: Attribute ; def deptName(implicit a: DeptName) = a.value
    }
    trait GroupS { 
        type GroupID <: Attribute ;  def groupID(implicit a: GroupID) = a.value
        type DeptName <: Attribute ; def deptName(implicit a: DeptName) = a.value
    }
    trait BudgetS { 
        type BudgetID <: Attribute ; def budgetID(implicit a: BudgetID) = a.value
        type DeptName <: Attribute ; def deptName(implicit a: DeptName) = a.value
    }


    // -------- Target Schema

    trait DepartmentT { 
        type DeptName <: Attribute ; def deptName(implicit a: DeptName) = a.value
        type DeptID <: Attribute ; def deptID(implicit a: DeptID) = a.value
    }
    trait GroupT { 
        type GroupID <: Attribute ; def groupID(implicit a: GroupID) = a.value
        type DeptID <: Attribute ; def deptID(implicit a: DeptID) = a.value
        type Leader <: Attribute ; def leader(implicit a: Leader) = a.value
    }
    trait BudgetT { 
        type BudgetID <: Attribute ; def budgetID(implicit a: BudgetID) = a.value
        type DeptID <: Attribute ; def deptID(implicit a: DeptID) = a.value
        type Amount <: Attribute ; def amount(implicit a: Amount) = a.value
    }


    // -------- New Attributes Generators (DeptID, Leader, Amount)

    trait Fd[DeptName <: Attribute] extends DepFn1[DeptName] { type Out <: Attribute }
    trait Fl[DeptName <: Attribute, GroupID <: Attribute] extends DepFn2[DeptName, GroupID] { type Out <: Attribute }
    trait Fa[DeptName <: Attribute, BudgetID <: Attribute] extends DepFn2[DeptName, BudgetID] { type Out <: Attribute }

    // Generator DeptID (0 as default value)

    object Fd {
        type Gen[DeptName <: Attribute, Result <: Attribute] = Fd[DeptName] { type Out = Result }

        def apply[DeptName <: Attribute](fd: Fd[DeptName]): Gen[DeptName, fd.Out] = fd

        implicit def fd[DeptName <: Attribute]: Gen[DeptName, Witness.Aux[Witness.`0`.T]] = 
            new Fd[DeptName] { type Out = Witness.Aux[Witness.`0`.T] ; def apply(attr: DeptName) = Witness(0) }
    }

    // Generator Leader (Jean-Pierre as default value)

    object Fl {
        type Gen[DeptName <: Attribute, GroupID <: Attribute, Result <: Attribute] = Fl[DeptName, GroupID] { type Out = Result }

        def apply[DeptName <: Attribute, GroupID <: Attribute](fl: Fl[DeptName, GroupID]): Gen[DeptName, GroupID, fl.Out] = fl
        
        implicit def fl[DeptName <: Attribute, GroupID <: Attribute]: Gen[DeptName, GroupID, Witness.Aux[Witness.`"Jean-Pierre"`.T]] = 
            new Fl[DeptName, GroupID]  { type Out = Witness.Aux[Witness.`"Jean-Pierre"`.T] ; def apply(attr1: DeptName, attr2: GroupID) = Witness("Jean-Pierre") }
    }

    // Generator Amount (12345.6 as default value)

    object Fa {
        type Gen[DeptName <: Attribute, BudgetID <: Attribute, Result <: Attribute] = Fa[DeptName, BudgetID] { type Out = Result }

        def apply[DeptName <: Attribute, BudgetID <: Attribute](fa: Fa[DeptName, BudgetID]): Gen[DeptName, BudgetID, fa.Out] = fa

        implicit def fa[DeptName <: Attribute, BudgetID <: Attribute]: Gen[DeptName, BudgetID, Witness.Aux[Witness.`12345.6`.T]] = 
            new Fa[DeptName, BudgetID]  { type Out = Witness.Aux[Witness.`12345.6`.T] ; def apply(attr1: DeptName, attr2: BudgetID) = Witness(12345.6) }
    }


    // -------- Schema Mapping Department

    def sigma1[NewDeptID <: Attribute](
        // Source Schema
        ds: DepartmentS
    )(
        // Generators
        implicit
        fd: Fd.Gen[ds.DeptName, NewDeptID]
    ):  
        // Target Schema
        DepartmentT { 
            type DeptName = ds.DeptName 
            type DeptID = NewDeptID 
        } = 
            new DepartmentT { type DeptName = ds.DeptName ; type DeptID = NewDeptID }


    // -------- Schema Mapping Group

    def sigma2[NewDeptID <: Attribute, NewLeader <: Attribute](
        // Source Schemas
        ds: DepartmentS, 
        gs: GroupS
    )(
        // Generators
        implicit
        fd: Fd.Gen[ds.DeptName, NewDeptID],
        fl: Fl.Gen[ds.DeptName, gs.GroupID, NewLeader]
    ): 
        // Target Schema
        GroupT { 
            type GroupID = gs.GroupID 
            type DeptID = NewDeptID 
            type Leader = NewLeader 
        } = 
            new GroupT { type GroupID = gs.GroupID ; type DeptID = NewDeptID ; type Leader = NewLeader }


    // -------- Schema Mapping Budget

    def sigma3[NewDeptID <: Witness, NewAmount <: Witness](
        // Source Schemas
        ds: DepartmentS, 
        bs: BudgetS
    )(
        // Generators
        implicit
        fd: Fd.Gen[ds.DeptName, NewDeptID],
        fa: Fa.Gen[ds.DeptName, bs.BudgetID, NewAmount]
    ): 
        // Target Schema
        BudgetT { 
            type BudgetID = bs.BudgetID 
            type DeptID = NewDeptID 
            type Amount = NewAmount 
        } = 
            new BudgetT { type BudgetID = bs.BudgetID ; type DeptID = NewDeptID ; type Amount = NewAmount }


    // -------- Full Schema Mapping

    def micro (
        // Source Schemas
        ds: DepartmentS, 
        gs: GroupS, 
        bs: BudgetS
    ) = (
        // Target Schema
        sigma1(ds), 
        sigma2(ds, gs), 
        sigma3(ds, bs)
    )


    // -------- Print the Full Schemas (Source and Target)

    def printSourceSchema (
        // Source Schemas
        ds: DepartmentS, 
        gs: GroupS, 
        bs: BudgetS
    )(
        // Value Extractors for Singletons
        implicit 
        deptName: ds.DeptName,
        groupID: gs.GroupID,
        budgetID: bs.BudgetID
    ) = 
        println(s"\nDepartment(DeptName = ${deptName.value}) \nGroup(GroupID = ${groupID.value}, DeptName = ${deptName.value}) \nBudget(BudgetID = ${budgetID.value}, DeptName = ${deptName.value})")

    def printTargetSchema (
        // Source Schemas
        dt: DepartmentT, 
        gt: GroupT, 
        bt: BudgetT
    )(
        // Value Extractors for Singletons
        implicit 
        deptName: dt.DeptName,
        deptID: dt.DeptID,
        leader: gt.Leader,
        groupID: gt.GroupID,
        budgetID: bt.BudgetID,
        amount: bt.Amount
    ) = 
    println(s"\nDepartment(DeptName = ${deptName.value}, DeptID = ${deptID.value}) \nGroup(GroupID = ${groupID.value}, DeptID = ${deptID.value}, Leader = ${leader.value}) \nBudget(BudgetID = ${budgetID.value}, DeptID = ${deptID.value}, Amount = ${amount.value})")


    // -------- Main

    def tests_tgds() = {
        // Values in Sources
        val sourceDepartment = new DepartmentS { type DeptName = Witness.Aux[Witness.`"ventes"`.T] }
        val sourceGroup = new GroupS { type GroupID = Witness.Aux[Witness.`1`.T] ; type DeptName = Witness.Aux[Witness.`"ventes"`.T] }
        val sourceBudget = new BudgetS { type BudgetID = Witness.Aux[Witness.`9`.T]  ; type DeptName = Witness.Aux[Witness.`"ventes"`.T] }

        println("\n---- Data in Source Tables:")
        printSourceSchema(sourceDepartment, sourceGroup, sourceBudget)

        // Schema Mapping
        val (targetDepartment, targetGroup, targetBudget) = micro(sourceDepartment, sourceGroup, sourceBudget)

        // Values in Targets
        println("\n---- Data in Target Tables:")
        printTargetSchema(targetDepartment, targetGroup, targetBudget)
    }
}