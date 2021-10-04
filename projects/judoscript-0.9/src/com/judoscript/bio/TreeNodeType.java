/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 08-18-2002  JH   Inception.
 * 12-20-2002  JH   Added sortChildren(), sortChildrenWholeTree()
 *                  and transposeTree() user methods.
 * 06-06-2003  JH   Renamed to TreeNodeType.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.bio;

import java.util.*;
import com.judoscript.*;
import com.judoscript.util.Queue;
import com.judoscript.util.AssociateList;


public class TreeNodeType extends UserDefined.Type
{
  public static final TreeNodeType TYPE = new TreeNodeType();
  static final ConstString.InternalName CHILDREN_NAME    = new ConstString.InternalName("_?tnt?_chn");
  static final ConstString.InternalName PARENT_NODE_NAME = new ConstString.InternalName("_?tnt?_pnt");
  static final ConstString.InternalName LEVEL_NAME       = new ConstString.InternalName("_?tnt?_lvl");

  public TreeNodeType() {
    super("TreeNode",null);

    try {

      addFunction("addChild",
        new FunctionBase() {
          public Variable invoke(Expr[] params, int[] types) throws Throwable {
            if (params.length == 0) return ValueSpecial.UNDEFINED;
            UserDefined _this = (UserDefined)RT.getThisObject();
            _Array arr;
            if (_this.exists(CHILDREN_NAME)) {
              arr = (_Array)_this.resolve(CHILDREN_NAME);
            } else {
              arr = new _Array();
              _this.setVariable(CHILDREN_NAME,arr,0);
            }
            for (int i=0; i<params.length; ++i) {
              Variable v = params[i].eval();
              arr.invoke("append",new Expr[]{v},null);
              try { ((UserDefined)v).setVariable(PARENT_NODE_NAME,_this,0); } catch(Exception e) {}
            }
            return ValueSpecial.UNDEFINED;
          }
        }
      );
    
      addFunction("getChildren",
        new FunctionBase() {
          public Variable invoke(Expr[] params, int[] types) throws Throwable {
            UserDefined _this = (UserDefined)RT.getThisObject();
            if (_this.exists(CHILDREN_NAME)) return _this.resolve(CHILDREN_NAME);
            _Array arr = new _Array();
            _this.setVariable(CHILDREN_NAME,arr,0);
            return arr;
          }
        }
      );

      addFunction("isLeaf",
        new FunctionBase() {
          public Variable invoke(Expr[] params, int[] types) throws Throwable {
            UserDefined _this = (UserDefined)RT.getThisObject();
            if (!_this.exists(CHILDREN_NAME)) return ConstInt.TRUE;
            _Array arr = (_Array)_this.resolveVariable(CHILDREN_NAME);
            return ConstInt.getBool(arr.size() == 0);
          }
        }
      );

      addFunction("sortChildren",
        new FunctionBase() {
          public Variable invoke(Expr[] params, int[] types) throws Throwable {
            UserDefined _this = (UserDefined)RT.getThisObject();
            if (!_this.exists(CHILDREN_NAME)) return ValueSpecial.UNDEFINED;
            _Array arr = (_Array)_this.resolveVariable(CHILDREN_NAME);
            arr.sort(params, null);
            return arr;
          }
        }
      );

      addFunction("sortChildrenWholeTree",
        new FunctionBase() {
          public Variable invoke(Expr[] params, int[] types) throws Throwable {
            UserDefined _this = (UserDefined)RT.getThisObject();
            Variable v = _this.invoke("sortChildren", params, types);
            if (v.isNil()) return _this;
            _Array arr = (_Array)v;
            for (int i=0; i<arr.size(); ++i)
              ((UserDefined)arr.resolve(i)).invoke("sortChildrenWholeTree", params, types);
            return _this;
          }
        }
      );

      addFunction("getParent",
        new FunctionBase() {
          public Variable invoke(Expr[] params, int[] types) throws Throwable {
            UserDefined _this = (UserDefined)RT.getThisObject();
            return _this.resolve(PARENT_NODE_NAME);
          }
        }
      );

      addFunction("setParent",
        new FunctionBase() {
          public Variable invoke(Expr[] params, int[] types) throws Throwable {
            UserDefined _this = (UserDefined)RT.getThisObject();
            return _this.setVariable(PARENT_NODE_NAME,(params.length==0)?params[0].eval():ValueSpecial.NIL,0);
          }
        }
      );

      addFunction("isTop",
        new FunctionBase() {
          public Variable invoke(Expr[] params, int[] types) throws Throwable {
            UserDefined _this = (UserDefined)RT.getThisObject();
            return ConstInt.getBool(!_this.exists(PARENT_NODE_NAME));
          }
        }
      );

      addFunction("getTop",
        new FunctionBase() {
          public Variable invoke(Expr[] params, int[] types) throws Throwable {
            UserDefined _this = (UserDefined)RT.getThisObject();
            return !_this.exists(PARENT_NODE_NAME) ? _this :
                     _this.resolve(PARENT_NODE_NAME).invoke("getTop", JudoUtil.emptyExprs,null);
          }
        }
      );

      addFunction("getLevel",
        new FunctionBase() {
          public Variable invoke(Expr[] params, int[] types) throws Throwable {
            UserDefined _this = (UserDefined)RT.getThisObject();
            if (!_this.exists(PARENT_NODE_NAME)) return ConstInt.ZERO;
            if (!_this.exists(LEVEL_NAME)) {
              Variable v = _this.resolve(PARENT_NODE_NAME).invoke("getLevel",JudoUtil.emptyExprs,null);
              _this.setVariable(LEVEL_NAME, ConstInt.getInt(v.getLongValue()+1),0);
            }
            return _this.resolve(LEVEL_NAME);
          }
        }
      );

      addFunction("getCascade",
        new FunctionBase() {
          public Variable invoke(Expr[] params, int[] types) throws Throwable {
            UserDefined _this = (UserDefined)RT.getThisObject();
            if (params.length <= 1) return ValueSpecial.UNDEFINED;
            String field = params[0].getStringValue();
            if (_this.exists(field)) return _this.resolve(field);
            if (_this.exists(PARENT_NODE_NAME))
              return _this.resolve(PARENT_NODE_NAME).invoke("getCascade",params,types);
            return (params.length >= 3) ? params[2].eval() : ValueSpecial.UNDEFINED;
          }
        }
      );

      addFunction("getPath",
        new FunctionBase() {
          public Variable invoke(Expr[] params, int[] types) throws Throwable {
            UserDefined _this = (UserDefined)RT.getThisObject();
            _Array arr = new _Array();
            Variable node = _this.invoke("getParent",null,null);
            while (node != null && (node instanceof UserDefined)) {
              arr.append(node);
              node = ((UserDefined)node).invoke("getParent",JudoUtil.emptyExprs,null);
            }
            return arr.invoke("reverse",null,null);
          }
        }
      );

      addFunction("hasInPath",
        new FunctionBase() {
          public Variable invoke(Expr[] params, int[] types) throws Throwable {
            if (params == null || params.length == 0) return ConstInt.FALSE;
            UserDefined _this = (UserDefined)RT.getThisObject();
            Variable checked = params[0].eval();
            Variable node = _this;
            while (node != ValueSpecial.NIL) {
              if (checked == node) return ConstInt.TRUE;
              node = node.invoke("getParent",JudoUtil.emptyExprs,null);
            }
            return ConstInt.FALSE;
          }
        }
      );

      addFunction("find",
        new FunctionBase() {
          public Variable invoke(Expr[] params, int[] types) throws Throwable {
            if (params == null || params.length == 0) return ValueSpecial.UNDEFINED;
            String fxnName = null;
            try {
              fxnName = ((AccessFunction)params[0].eval()).getName();
            } catch(ClassCastException e) {
              ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
                                   "treeNode's find() method takes a comparison function.");
            }
            UserDefined _this = (UserDefined)RT.getThisObject();
            Stack stack = new Stack();
            stack.push(_this);
            while (!stack.isEmpty()) {
              Variable node = (Variable)stack.pop();
              if ( RT.getScript().invoke(fxnName,new Expr[]{node},null).getBoolValue() )
                return node;
            }
            return ValueSpecial.UNDEFINED;
          }
        }
      );

      addFunction("findByKey",
        new FunctionBase() {
          public Variable invoke(Expr[] params, int[] types) throws Throwable {
            if (params == null || params.length == 0) return ValueSpecial.UNDEFINED;
            UserDefined _this = (UserDefined)RT.getThisObject();
            String key = params[0].getStringValue();
            Queue queue = new Queue();
            queue.enq(_this);
            while (!queue.isEmpty()) {
              Variable node = (Variable)queue.deq();
              if ( key.equals(node.invoke("getKey",JudoUtil.emptyExprs,null).getStringValue()) )
                return node;
              Variable v = node.resolveVariable(CHILDREN_NAME);
              if (!v.isNil()) {
                List l = (List)v.getObjectValue();
                for (int i=0; i<l.size(); ++i)
                  queue.enq(l.get(i));
              }
            }
            return ValueSpecial.UNDEFINED;
          }
        }
      );

      addFunction("getKey",
        new FunctionBase() {
          public Variable invoke(Expr[] params, int[] types) throws Throwable {
            return RT.getThisObject()
                      .invoke("toString",JudoUtil.emptyExprs,null); // default same as toString().
          }
        }
      );

      addFunction("dfsAllNodes",
        new FunctionBase() {
          public Variable invoke(Expr[] params, int[] types) throws Throwable {
            _Array ret = new _Array();
            Stack stack = new Stack();
            UserDefined _this = (UserDefined)RT.getThisObject();
            stack.push(_this);
            while (!stack.isEmpty()) {
              Variable node = (Variable)stack.pop();
              ret.append(node);
              Variable v = node.resolveVariable(CHILDREN_NAME);
              if (!v.isNil()) {
                List l = (List)v.getObjectValue();
                for (int i=0; i<l.size(); ++i)
                  stack.push(l.get(i));
              }
            }
            return ret;
          }
        }
      );

      addFunction("bfsAllNodes",
        new FunctionBase() {
          public Variable invoke(Expr[] params, int[] types) throws Throwable {
            _Array ret = new _Array();
            Queue queue = new Queue();
            UserDefined _this = (UserDefined)RT.getThisObject();
            queue.enq(_this);
            while (!queue.isEmpty()) {
              Variable node = (Variable)queue.deq();
              ret.append(node);
              Variable v = node.resolveVariable(CHILDREN_NAME);
              if (!v.isNil()) {
                List l = (List)v.getObjectValue();
                for (int i=0; i<l.size(); ++i)
                  queue.enq(l.get(i));
              }
            }
            return ret;
          }
        }
      );

      addFunction("transposeTree",
        new FunctionBase() {
          public Variable invoke(Expr[] params, int[] types) throws Throwable {
            if (params==null || params.length==0) return ValueSpecial.UNDEFINED;
            String typename = params[0].getStringValue();
            UserDefined.UserType ut = RT.getScript().getObjectType(typename);
            if (ut instanceof UserDefined.Type) {
              UserDefined.Type t = (UserDefined.Type)ut;
              UserDefined _this = (UserDefined)RT.getThisObject();
              _Array arr = (_Array)_this.invoke("bfsAllNodes", null, null);
              Expr[] ea = new Expr[]{ JudoUtil.toVariable(typename) };
              for (int i=0; i<arr.size(); ++i)
                ((UserDefined)arr.resolve(i)).invoke("transpose", ea, null);
              return _this;
            }
            ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
                                 "Unable to transpose the tree to class " + typename + '.');
            return null;
          }
        }
      );

    } catch(Throwable e) {
      RT.logger.error("TreeNodeType. Shouldn't happen.", e);
    }
  }

  public boolean isBuiltin() { return true; }

  public static UserDefined createTreeNode(AssociateList data) throws Throwable {
    UserDefined udo = (UserDefined)TYPE.create();
    udo.init(data,null);
    return udo;
  }

  public static UserDefined createTreeNode(String name, Object val) throws Throwable {
    AssociateList al = new AssociateList();
    al.add(name, JudoUtil.toVariable(val));
    return createTreeNode(al);
  }

  public static UserDefined createTreeNode(String n1, Object v1, String n2, Object v2)
                            throws Throwable
  {
    AssociateList al = new AssociateList();
    al.add(n1, JudoUtil.toVariable(v1));
    al.add(n2, JudoUtil.toVariable(v2));
    return createTreeNode(al);
  }

} // end of class TreeNode.
