package memorymanagement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import memorymanagement.Memory;
import processesmanagement.ControlBlock;

public class PageTable {
	protected static int[] frameNumber;
	protected static int pagesRequired;
	private static Boolean[] inRAM;
	
	public PageTable(String fileName, int processSize) {
		if(processSize%Memory.FRAME_SIZE==0)
			pagesRequired=processSize/Memory.FRAME_SIZE;
		else
			pagesRequired=processSize/Memory.FRAME_SIZE+1;
		System.out.println("Pages Required: " + pagesRequired);
		initInRAM();
		initFrameNumber();
		setVirtualBase();
		writeFromFileToVirtualMemory(fileName, processSize);
	}
	
	public static void print() {
		System.out.println("\nPage Table ID:" + ControlBlock.ID); // TODO - ControlBlock
		System.out.println("Frame number: ");
		for(int e : frameNumber)
			System.out.print(e + " ");
		System.out.println("\nIn RAM: ");
		for(Boolean e : inRAM)
			System.out.print(e + " ");
		System.out.println("\n");
	}
	
	private void initInRAM() {
		inRAM = new Boolean[pagesRequired];
		for(int i=0;i<pagesRequired;i++)
			inRAM[i] = false;
	}
	
	private void initFrameNumber() {
		frameNumber = new int[pagesRequired];
		for(int i=0;i<pagesRequired;i++)
			frameNumber[i] = -1;
	}
	
	public int getVirtualBase() {
		// TODO?
		return 0;
	}
	
	private void setVirtualBase() {
		// TODO
		ControlBlock.virtualBase = 2;
	}
	
	public void writeToVirtualMemory(char[] program, int processSize) {
		Memory.writeToVirualMemory(ControlBlock.virtualBase, program, processSize); 
	}	
	
	public void writeFromFileToVirtualMemory(String fileName, int processSize) {
		char[] program;
		StringBuilder sb = new StringBuilder();
		int logicalAdress = 0;
		File file = new File("src/" + fileName);
			if (!file.exists()) {
				System.out.println(fileName + " does not exist.");
				return;
			}
			if (!(file.isFile() && file.canRead())) {
				System.out.println(file.getName() + " cannot be read from.");
				return;
			}
			try {
				FileInputStream fis = new FileInputStream(file);
				if(processSize<fis.available()) {
					System.out.println("process size is too small");
			//		return;
				}
				char current;
				while (logicalAdress < processSize) {
					current = (char) fis.read();
					sb.append(current);
// trzeba uwzglednic, ze kazda nowa linia (zamieniona na ' ') ma swoj adres logiczny		        
					if(current != '\n')
						logicalAdress++;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		String everything = sb.toString();
		String result = everything.replaceAll("[\\t\\n\\r]+"," ");
		program = result.toCharArray();
		Memory.writeToVirualMemory(ControlBlock.virtualBase, program, processSize); //virtualBase wziac z tablicy PCB? z obecnie wykonywanego procesu?
	}

	public  char readFromMemory(int logicalAdress) {
		if(isFrameInRAM(getVirtualFrame(logicalAdress))) {
			// TODO: set bit innego procesu		
			int ID = ControlBlock.ID; 	// TODO: wziac ID z obecnego procesu
			int frameRAM = frameNumber[getVirtualFrame(logicalAdress)];
			for(FIFOFrame e : Memory.FIFO)
				if(e.ID == ID && e.number == frameRAM)
					e.bit=true;
			return getCharFromRAM(logicalAdress);
		}
		else {
			writeFrameToRAM(getVirtualFrame(logicalAdress));
			return getCharFromRAM(logicalAdress);
		}
	}
	
	private static int getVirtualFrame(int logicalAdress) {
		return logicalAdress/Memory.FRAME_SIZE;
	}
	
	private static int getVirtualOffset(int logicalAdress) {
		return logicalAdress%Memory.FRAME_SIZE;
	}
	
	private static boolean isFrameInRAM(int virtualFrame) {
		return inRAM[virtualFrame]==true;
	}
	
	private static char getCharFromRAM(int logicalAdress) {
		char character = 0;
		int frameInRAM = frameNumber[getVirtualFrame(logicalAdress)];
		int offset = getVirtualOffset(logicalAdress);
		character = Memory.getCharFromRAM(frameInRAM, offset);
		return character;
	}
	
	private static void writeFrameToRAM(int frameVirtual) {
		if(Memory.isFreeFrame()) {
			int frameRAM = Memory.firstFreeFrame();
			Memory.writeFrameToRAM(frameVirtual, frameRAM);
			frameNumber[frameVirtual] = frameRAM;
			inRAM[frameVirtual] = true;
			addFrameToFIFO(frameRAM, ControlBlock.ID); // TODO: zmienic ControlBlock na element w tablicy procesow
		}
		else {
			int victimFrameFIFOIndex = getVictimFrame();
			int frameRAM = Memory.FIFO.get(victimFrameFIFOIndex).number;
			int ID = Memory.FIFO.get(victimFrameFIFOIndex).ID;				
			if(isPageInCurrentProcess(ID)) 
				removeEverythingAboutPageOfCurrentProcessFromRAM(frameRAM);
			else {
				// TODO: usu� z pagetable innego procesu
			}
			Memory.FIFO.remove(victimFrameFIFOIndex);
			
			Memory.writeFrameToRAM(frameVirtual, frameRAM);
			addEverythingAboutPageOfCurrentProcessToRAM(frameVirtual, frameRAM, ID);
		}
	}
	
	private static void removeEverythingAboutPageOfCurrentProcessFromRAM(int frameRAM) {
		// from RAM to virtual
		int frameVirtualToRewrite = frameNumber[frameRAM];
		if(frameVirtualToRewrite != -1)
			Memory.rewriteFromRAMToVirtualMemory(frameVirtualToRewrite, getVirtualFrameOfOtherProcess());
		
		//frameNumber, inRAM
		int indexOfFrameToClear = 0;
		for(int i=0; i<frameNumber.length; i++) {
			if(frameNumber[indexOfFrameToClear]==frameRAM) 
				break;
			else
				indexOfFrameToClear++;
		}
		frameNumber[indexOfFrameToClear] = -1;
		inRAM[indexOfFrameToClear] = false;
	}
	
	private static void addEverythingAboutPageOfCurrentProcessToRAM(int frameVirtual, int frameRAM, int ID) {
		frameNumber[frameVirtual] = frameRAM;
		inRAM[frameVirtual] = true;
		addFrameToFIFO(frameRAM, ID);
	}
	
	private static Boolean isPageInCurrentProcess(int IDfromFIFO) {
		// TODO
		return true;
	}

	private static void addFrameToFIFO(int frameRAM, int ID) {
		FIFOFrame newFrame = new FIFOFrame(frameRAM, ID);
		Memory.FIFO.add(newFrame);
	}
	
	private static int getVictimFrame() {
		Boolean found = false;
		int victimIndex = 0;
		while(!found){
			if(isBitZero(victimIndex)) 			
				found = true; 
			else {
				Memory.FIFO.get(victimIndex).bit = false;			
				victimIndex++;
				if(victimIndex >= Memory.FIFO.size())
					victimIndex = 0;
			}
		}
		return victimIndex;
	}
	
	private static Boolean isBitZero(int elementIndex) {
		return(!Memory.FIFO.get(elementIndex).bit);
	}
	
	private static int getVirtualFrameOfOtherProcess() {
		// TODO
		return 0;
	}
	
	public static void writeToMemory(int logicalAdress, char character) {
		if(isFrameInRAM(getVirtualFrame(logicalAdress))) {
			// TODO: set bit innego procesu?			
			int ID = ControlBlock.ID; 	// TODO: wziac ID z obecnego procesu
			int frameRAM = frameNumber[getVirtualFrame(logicalAdress)];
			for(FIFOFrame e : Memory.FIFO)
				if(e.ID == ID && e.number == frameRAM)
					e.bit=true;
			writeCharToRAM(logicalAdress, character);
		}
		else {
			writeFrameToRAM(getVirtualFrame(logicalAdress));
			writeCharToRAM(logicalAdress, character);
		}
	}
	
	private static void writeCharToRAM(int logicalAdress, char character) {
		int frameInRAM = frameNumber[getVirtualFrame(logicalAdress)];
		int offset = getVirtualOffset(logicalAdress);
		Memory.writeCharToRAM(frameInRAM, offset, character);

	}
}