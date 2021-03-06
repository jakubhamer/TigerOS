package processManagement;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import Communication.Communication;
import Interpreter.Interpreter;
import fileManagement.FileSystem;
import memorymanagement.Memory;
import processorManager.ProcessorManager;

public class ProcessManagment {
	static lista_procesow istniejace_procesy;

	public ProcessManagment(Memory memory) {
		istniejace_procesy = new lista_procesow(memory); // tworzy liste procesow
	} // na ktorej juz jest proces bezczynnosci

	public int create_process(String nname,int size, String file_nam, Memory memory) {

		istniejace_procesy.add_to_list(new process_control_block(nname,size, file_nam, memory));

		return 1;
	}

	public void delete_process(int id) {
		if (istniejace_procesy.delete_from_list(id) == 1)
			System.out.println("nie ma takiego procesu");
	}

	public void delete_process_iter(int iter) {
		istniejace_procesy.delete_on_iter(iter);
	}

	public void print_procesy() {
		System.out.println();
		for (int i = 0; i < istniejace_procesy.size(); i++) {
			istniejace_procesy.getPCB(i).print();
			System.out.println();
		}
	}

	public void print_procesy_gotowe() {
		System.out.println();
		for (int i = 0; i < istniejace_procesy.size(); i++) {
			if (istniejace_procesy.getPCB(i).getStan() == 0) {
				istniejace_procesy.getPCB(i).print();
				System.out.println();
			}
		}
	}

	public void print_procesy_oczekujace() {
		System.out.println();
		for (int i = 0; i < istniejace_procesy.size(); i++) {
			if (istniejace_procesy.getPCB(i).getStan() == 1) {
				istniejace_procesy.getPCB(i).print();
				System.out.println();
			}
		}
	}

	public void usun_skonczone() {
		for (int i = 0; i < istniejace_procesy.size(); i++) {
			if (istniejace_procesy.getPCB(i).getStan() == 2)
				{istniejace_procesy.getPCB(i).pageTable.deleteFromMemory(istniejace_procesy.getPCB(i).getID());
				delete_process_iter(i);}
		}
	}

	public void sortuj_procesy() {
		istniejace_procesy.gotowe_na_poczatek();
	}

	public void uporzadkuj_procesy() {
		usun_skonczone();
		sortuj_procesy();
	}

	public static process_control_block getPCB(int ID) {
		return istniejace_procesy.getPCB_by_ID(ID);
	}

	public lista_procesow getIstniejaceProcesy() {
		return istniejace_procesy;
	}

}
