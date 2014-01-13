package com.zyh.test.adx.request.stat;

import java.io.File;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

/**
 * 统计谷歌请求中带有allowed_vendor_type的相关数据
 * @author zhyhang
 *
 */
public class AllowedVendorTypeStatic {
	private final static Logger logger=Logger.getGlobal();
	static{
		logger.addHandler(new ConsoleHandler());
		logger.setUseParentHandlers(false);
	}

	public static void main(String[] args) throws Exception {
		long requests=0;
		long hasAllows=0;
		long allowed113=0;
		if(args.length==0){
			logger.info("please specify the full file name.");
			return;
		}
		File f=new File(args[0]);
		if(!f.exists()){
			logger.info(args[0]+" is not found!");
			return;
		}
		Scanner scanner=new Scanner(f);
		boolean newSlot=false;
		while(scanner.hasNextLine()){
			String line=scanner.nextLine().trim();
			if(line.startsWith("adslot")){
				requests++;
				newSlot=true;
			}
			if(newSlot && line.startsWith("allowed_vendor_type")){
				hasAllows++;
				newSlot=false;
			}
			if(line.startsWith("allowed_vendor_type: 113")){
				allowed113++;
			}
		}
		scanner.close();
		logger.info("request: "+requests+"; has allowed_vendor_type: "+hasAllows+"; has allowed_vendor_type: 113:"+allowed113);
	}

}