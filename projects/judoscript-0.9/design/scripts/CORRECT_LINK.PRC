CREATE OR REPLACE PROCEDURE correct_link  
(	   Inv_Wrong_Linked IN cib_order_line.invoice_number%type,
	   Inv_Correct 		IN cib_order_line.invoice_number%type
)
AS
Inv_Wrong_Linked_Present NUMBER(5);
Inv_Correct_Present NUMBER(5);
Inv_Wrong_Linked_Maint_Present NUMBER(5);
Inv_Correct_Lic_Present NUMBER(5);
Temp_PS_Prod_Id_Present NUMBER(5);
temp_lic_line_no NUMBER(5);

temp_order_no cib_order_line.ORDER_NO%TYPE;
temp_line_no cib_order_line.ORDER_INT_LINE_NO%TYPE;
temp_prod_id cib_license.PS_PRODUCT_ID%TYPE;

TYPE Maint_Rec is RECORD
(
 order_line cib_maint.ORDER_LINE_ID%TYPE,
 license_id cib_maint.CIB_LICENSE_SNAPSHOT_ID%TYPE,
 maintenance_id cib_maint.MAINTENANCE_ID%TYPE
 
);

TYPE Maint_Array is TABLE of Maint_Rec;
M_Array Maint_Array;

cursor Maint_Cursor is 
select order_line_id,cib_license_snapshot_id,maintenance_id
from cib_maint where order_line_id IN
(select order_line_id from cib_order_line where invoice_number=Inv_Wrong_Linked);

type License_Rec is RECORD
(
  lic_snap_id cib_license.CIB_LICENSE_SNAPSHOT_ID%type,
  cur_maint_id cib_license.CURRENT_MAINTENANCE_ID%type,
  lic_prod_id cib_license.PS_PRODUCT_ID%type,
  from_lic_act_id  cib_license.FROM_CIB_LICENSE_ACTIVITY_ID%type
);
type License_Array is table of Maint_Rec;
L_Array License_Array;

cursor License_Cursor is
select cib_license_snapshot_id,current_maintenance_id,ps_product_id,from_cib_license_activity_id 
 from cib_license where from_cib_license_activity_id in
(select cib_license_activity_id from cib_lic_activity where order_line_id in 
(select order_line_id from cib_order_line where invoice_number =Inv_Correct));


BEGIN

	 dbms_output.put_line('Original (Wrongly linked) invoice -'||Inv_Wrong_Linked);
 	 dbms_output.put_line('Invoice to which linking is to be done -'||Inv_Correct);
 	 
	 -- check if the original invoice exists in CIB
	 select count(*) into Inv_Wrong_Linked_Present from cib_order_line where invoice_number=Inv_Wrong_Linked;
	 
	 dbms_output.put_line(' '||to_char(Inv_Wrong_Linked_Present)||' No ofTotal Order lines in cib_order_line for original Inv-'||Inv_Wrong_Linked);	 
	 
	 IF (Inv_Wrong_Linked_Present = 0) THEN
	  -- raise exception..orig.invoice does not exist 
	    dbms_output.put_line('The original Inv  does not exist- Inv#'||Inv_Wrong_Linked); 
	  raise_application_error(-20001,'The original Inv  does not exist- Inv#'||Inv_Wrong_Linked);	
	 END IF; 
	  
	 -- check if the invoice to be linked exists in CIB 
  	 select count(*) into Inv_Correct_Present from cib_order_line where invoice_number=Inv_Correct;

	 dbms_output.put_line(' '||to_char(Inv_Correct_Present)||' No of Total Order lines present in cib_order_line for Inv to be linked to Inv-'||Inv_Correct);
	  
	 IF (Inv_Correct_Present = 0) THEN
	   -- raise exception..invoice to be linked to does not exist
	   dbms_output.put_line('The Inv to be linked to does not exist- Inv#'||Inv_Correct);
   	  raise_application_error(-20001,'The Inv to be linked to does not exist- Inv#'||Inv_Correct);
	 END IF; 
	 	
	-- check whether maintenance lines are present in the original invoice
	select count(*) into Inv_Wrong_Linked_Maint_Present from cib_maint where order_line_id IN
	(select order_line_id from cib_order_line where invoice_number=Inv_Wrong_Linked);
	
    dbms_output.put_line(' '||to_char(Inv_Wrong_Linked_Maint_Present)||' No of Maintenance order lines present in cib_maint for original Inv-'||Inv_Wrong_Linked);
	
	IF(Inv_Wrong_Linked_Maint_Present =0)THEN
	 -- raise exception..no maint lines present in orig. invoice 
	 dbms_output.put_line('NO Maintenance order lines present in original invoice - Inv-'||Inv_Wrong_Linked);
	 raise_application_error(-20001,'NO Maintenance order lines present in original invoice - Inv-'||Inv_Wrong_Linked);
    END IF;
	    
	 -- check whether license lines are present in the invoice to be linked  
	select count(*) into Inv_Correct_Lic_Present from cib_license where from_cib_license_activity_id in
	(select cib_license_activity_id from cib_lic_activity where order_line_id in 
	(select order_line_id from cib_order_line where invoice_number =Inv_Correct));
	
    dbms_output.put_line(' '||to_char(Inv_Correct_Lic_Present)||' No of License order lines present in cib_License for Inv to be lined to Inv-'||Inv_Correct);
	
	IF(Inv_Correct_Lic_Present= 0) THEN
	 -- raise exception..no lic lines present in correct Invoice 
	 dbms_output.put_line('NO license lines present in Inv to be linked to Inv-'||Inv_Correct);
	 raise_application_error(-20001,'NO license lines present in Inv to be linked to Inv-'||Inv_Correct);
	END IF; 
	
	-- check whether the number of license lines and the number of maintenance lines match  
	IF(Inv_Correct_Lic_Present <> Inv_Wrong_Linked_Maint_Present) THEN
	 -- raise exception ..no of licences and no of maintenances do not match 	 
	 dbms_output.put_line('No of License order lines in INv-'||Inv_Correct||' and no of Maintenance order lines in Inv-'||Inv_Wrong_Linked||' do not match');
	 raise_application_error(-20001,'No of License order lines in Inv-'||Inv_Correct||' and no of Maintenance order lines in Inv-'||Inv_Wrong_Linked||' do not match');
	END IF;  	
	
	-- Start the transaction
	SAVEPOINT start_transaction;
	-- LOOP for all the maintenance lines 
	  for M_Array in Maint_Cursor loop
	  	  --dbms_output.PUT_LINE('Looping for wrongly linked inv order_line_id'||M_Array.order_line_id);
		  	
		-- get order_no and order_int_line no from cib_order_line to query PS database for mantained product id 
		SELECT order_no,order_int_line_no into temp_order_no,temp_line_no from cib_order_line where order_line_id=M_Array.order_line_id;
		dbms_output.PUT_LINE('  Looping for Inv-'||Inv_Correct||'order line no='||to_char(temp_line_no)||'(Order line id -'||M_Array.order_line_id||')');

		-- a DB LINK is needed to connect to PS Database. Assumed that link exists and is named FSEOCIB 		
		select count(*) into Temp_PS_Prod_id_Present from sysadm.ps_bea_cib_ext_vw@FSEOCIB where order_no=temp_order_no and order_int_line_no=temp_line_no;		  	 
		
		IF(Temp_PS_Prod_id_Present =0)THEN
		 -- raise exception ...record not found in Peoplesoft 
		 ROLLBACK TO start_transaction;
		 dbms_output.PUT_LINE('  Record NOT found in PS Order No='||temp_order_no||'(Inv-'||Inv_Wrong_Linked||') line no='||to_char(temp_line_no)||' Matching Cannot proceed - Transaction Rolled Back');
		 raise_application_error(-20001,'Record NOT found in PS Order No='||temp_order_no||'(Inv-'||Inv_Wrong_Linked||') line no='||to_char(temp_line_no)||' Matching Cannot proceed - Transaction Rolled Back');				
        END IF;	
		
		dbms_output.PUT_LINE('  Record found in PS for Order No='||temp_order_no||'(Inv-'||Inv_Wrong_Linked||') and line no='||to_char(temp_line_no)); 

		-- quering PS for maintained product ID 
		select maintained_ps_product_id into temp_prod_id from sysadm.ps_bea_cib_ext_vw@FSEOCIB where order_no=temp_order_no and order_int_line_no=temp_line_no;		
		dbms_output.PUT_LINE('  For this maintenence order line maintained prod is (from PS)='||temp_prod_id);		
	
		-- Loop for all the license lines 
		<<Inner_Loop>>
		for L_Array in License_Cursor loop
		dbms_output.PUT_LINE('     Searching in Licence order lines of Inv-'||Inv_Correct||' For Prod='||temp_prod_id||' Prod id of this line is'||L_Array.PS_PRODUCT_ID);  
						
		IF(L_Array.ps_product_id = temp_prod_id)THEN
			-- match is found -- do update 
			select order_int_line_no into temp_lic_line_no from cib_order_line where order_line_id in (select order_line_id from cib_lic_activity where cib_license_activity_id=L_Array.from_cib_license_activity_id); 
			dbms_output.put_line('    Match Found - Product IDs are same for Maintenance order line (Inv-'||Inv_Wrong_Linked||' order line no-'||to_char(temp_line_no)||')');
			dbms_output.put_line('    AND License order line (Inv-'||Inv_Correct||' order line no-'||temp_lic_line_no||')'); 
			 update cib_maint set cib_license_snapshot_id = L_Array.CIB_LICENSE_SNAPSHOT_ID where maintenance_id = M_Array.MAINTENANCE_ID;
			 update cib_license set current_maintenance_id = M_Array.MAINTENANCE_ID where cib_license_snapshot_id=L_ARRAY.CIB_LICENSE_SNAPSHOT_ID;
			dbms_output.PUT_LINE('    update done');
			
			EXIT Inner_Loop;
		END IF;		
		END LOOP;
	
	END LOOP;	
	 commit;  -- COMMIT AFTER ALL THE LINES ARE FIXED 	
	dbms_output.PUT_LINE('commit executed');
END;
/
