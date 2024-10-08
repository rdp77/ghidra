/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package agent.lldb.lldb;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.*;

import SWIG.*;
import ghidra.test.AbstractGhidraHeadlessIntegrationTest;
import ghidra.util.Msg;

@Ignore("deprecated")
public class LLDBTest extends AbstractGhidraHeadlessIntegrationTest {

	public static void assumeLibLoadable() {
		try {
			System.load("/usr/lib/liblldb.so");
			return;
		}
		catch (UnsatisfiedLinkError ex) {
		}
		try {
			System.load("/usr/lib/liblldb.dylib");
			return;
		}
		catch (UnsatisfiedLinkError ex) {
		}
		assumeTrue("lldb not available", false);
	}

	private SBDebugger sbd;

	@Before
	public void setUp() {
		assumeLibLoadable();
		SBDebugger.InitializeWithErrorHandling();
		sbd = SBDebugger.Create();
	}

	@Test
	public void testCanWeDoAnything() {
		System.err.println(sbd);
	}

	protected class ProcMaker implements AutoCloseable {
		public ProcMaker(String cmdLine) {
			this.cmdLine = cmdLine;
		}

		final String cmdLine;

		final CompletableFuture<Integer> procExit = new CompletableFuture<>();

		StringBuilder outputCapture = null;

		private SBProcess process;

		public void start() {
			/*
			client.setEventCallbacks(new NoisyDebugEventCallbacksAdapter(DebugStatus.NO_CHANGE) {
				@Override
				public DebugStatus createProcess(DebugProcessInfo debugProcessInfo) {
					super.createProcess(debugProcessInfo);
					procInfo.complete(debugProcessInfo);
					return DebugStatus.BREAK;
				}
			
				@Override
				public DebugStatus createThread(DebugThreadInfo debugThreadInfo) {
					super.createThread(debugThreadInfo);
					threadInfo.complete(debugThreadInfo);
					return DebugStatus.BREAK;
				}
			
				@Override
				public DebugStatus exitProcess(int exitCode) {
					super.exitProcess(exitCode);
					procExit.complete(exitCode);
					return DebugStatus.BREAK;
				}
			});
			client.setOutputCallbacks(new DebugOutputCallbacks() {
				@Override
				public void output(int mask, String text) {
					System.out.print(text);
					if (outputCapture != null) {
						outputCapture.append(text);
					}
				}
			});
			*/

			Msg.debug(this, "Starting " + cmdLine + " with client " + sbd);
			SBTarget target = sbd.CreateTarget(cmdLine);
			process = target.LaunchSimple(null, null, null);
			assertNotNull(process);
			System.out.println(DebugClient.getId(process) + ":" + process.GetUniqueID());
			SBProcessInfo pi = process.GetProcessInfo();
			assertNotNull(pi);
			SBThread thread = process.GetThreadAtIndex(0);
			assertNotNull(thread);
			System.out.println(DebugClient.getId(thread) + ":" + process.GetUniqueID());
			thread.Resume();
		}

		public void kill() {
			Msg.debug(this, "Killing " + cmdLine);
			process.Kill();
			try {
				process.wait();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public List<String> execCapture(String command) {
			try {
				outputCapture = new StringBuilder();
				//control.execute(command);
				return Arrays.asList(outputCapture.toString().split("\n"));
			}
			finally {
				outputCapture = null;
			}
		}

		@Override
		public void close() {
			StateType state = process.GetState();
			if (!state.equals(StateType.eStateExited)) {
				kill();
			}
		}
	}

	@Test
	public void testStart() {
		try (ProcMaker maker = new ProcMaker("/bin/echo")) {
			maker.start();
		}
	}

	/*
	@Test
	public void testPrintln() {
		CompletableFuture<String> cb = new CompletableFuture<>();
		client.setOutputCallbacks(new DebugOutputCallbacks() {
			@Override
			public void output(int mask, String text) {
				System.out.print(text);
				cb.complete(text);
			}
		});
		control.outln("Hello, World!");
		String back = cb.getNow(null);
		// NOTE: I'd like to be precise wrt/ new lines, but it seems to vary with version.
		assertEquals("Hello, World!", back.trim());
	}
	
	@Test
	public void testGetProcessSystemIds() {
		List<DebugRunningProcess> procs = client.getRunningProcesses(client.getLocalServer());
		System.out.println("Total: " + procs.size());
		procs.sort(null);
		for (DebugRunningProcess p : procs) {
			System.out.println("ID: " + p.getSystemId());
		}
	}
	
	@Test
	public void testGetProcessDescriptions() {
		List<DebugRunningProcess> procs = client.getRunningProcesses(client.getLocalServer());
		System.out.println("Total: " + procs.size());
		procs.sort(null);
		for (DebugRunningProcess p : procs) {
			try {
				System.out.println(p.getFullDescription());
			}
			catch (COMException e) {
				System.out.println("Error with PID " + p.getSystemId() + ": " + e.getMessage());
			}
		}
	}
	
	public static abstract class NoisyDebugEventCallbacksAdapter
			extends DebugEventCallbacksAdapter {
		final DebugStatus defaultStatus;
	
		public NoisyDebugEventCallbacksAdapter(DebugStatus defaultStatus) {
			this.defaultStatus = defaultStatus;
		}
	
		@Override
		public DebugStatus createProcess(DebugProcessInfo debugProcessInfo) {
			Msg.info(this, "createProcess: " + debugProcessInfo);
			return defaultStatus;
		}
	
		@Override
		public DebugStatus createThread(DebugThreadInfo debugThreadInfo) {
			Msg.info(this, "createThread: " + debugThreadInfo);
			return defaultStatus;
		}
	
		@Override
		public DebugStatus exitProcess(int exitCode) {
			Msg.info(this, "exitProcess: " + Integer.toHexString(exitCode));
			return defaultStatus;
		}
	
		@Override
		public DebugStatus breakpoint(DebugBreakpoint bp) {
			Msg.info(this, "breakpoint: " + bp);
			return defaultStatus;
		}
	
		@Override
		public DebugStatus changeDebuggeeState(BitmaskSet<ChangeDebuggeeState> flags,
				long argument) {
			Msg.info(this, "changeDebuggeeState: " + flags + ", " + argument);
			return defaultStatus;
		}
	
		@Override
		public DebugStatus changeEngineState(BitmaskSet<ChangeEngineState> flags, long argument) {
			Msg.info(this, "changeEngineState: " + flags + ", " + argument);
			return defaultStatus;
		}
	
		@Override
		public DebugStatus changeSymbolState(BitmaskSet<ChangeSymbolState> flags, long argument) {
			Msg.info(this, "changeSymbolState: " + flags + ", " + argument);
			return defaultStatus;
		}
	
		@Override
		public DebugStatus exception(DebugExceptionRecord64 exception, boolean firstChance) {
			Msg.info(this, "exception: " + exception + ", " + firstChance);
			return defaultStatus;
		}
	
		@Override
		public DebugStatus exitThread(int exitCode) {
			Msg.info(this, "exitThread: " + Integer.toHexString(exitCode));
			return defaultStatus;
		}
	
		@Override
		public DebugStatus loadModule(DebugModuleInfo debugModuleInfo) {
			Msg.info(this, "loadModule: " + debugModuleInfo);
			return defaultStatus;
		}
	
		@Override
		public DebugStatus sessionStatus(SessionStatus status) {
			Msg.info(this, "sessionStatus: " + status);
			return defaultStatus;
		}
	
		@Override
		public DebugStatus systemError(int error, int level) {
			Msg.info(this, "systemError: " + error + ", " + level);
			return defaultStatus;
		}
	
		@Override
		public DebugStatus unloadModule(String imageBaseName, long baseOffset) {
			Msg.info(this, "unloadModule: " + imageBaseName + ", " + baseOffset);
			return defaultStatus;
		}
	}
	
	protected class ProcMaker implements AutoCloseable {
		public ProcMaker(String cmdLine) {
			this.cmdLine = cmdLine;
		}
	
		final String cmdLine;
	
		final CompletableFuture<DebugProcessInfo> procInfo = new CompletableFuture<>();
		final CompletableFuture<DebugThreadInfo> threadInfo = new CompletableFuture<>();
		final CompletableFuture<Integer> procExit = new CompletableFuture<>();
	
		StringBuilder outputCapture = null;
	
		public void start() {
			client.setEventCallbacks(new NoisyDebugEventCallbacksAdapter(DebugStatus.NO_CHANGE) {
				@Override
				public DebugStatus createProcess(DebugProcessInfo debugProcessInfo) {
					super.createProcess(debugProcessInfo);
					procInfo.complete(debugProcessInfo);
					return DebugStatus.BREAK;
				}
	
				@Override
				public DebugStatus createThread(DebugThreadInfo debugThreadInfo) {
					super.createThread(debugThreadInfo);
					threadInfo.complete(debugThreadInfo);
					return DebugStatus.BREAK;
				}
	
				@Override
				public DebugStatus exitProcess(int exitCode) {
					super.exitProcess(exitCode);
					procExit.complete(exitCode);
					return DebugStatus.BREAK;
				}
			});
			client.setOutputCallbacks(new DebugOutputCallbacks() {
				@Override
				public void output(int mask, String text) {
					System.out.print(text);
					if (outputCapture != null) {
						outputCapture.append(text);
					}
				}
			});
	
			Msg.debug(this, "Starting " + cmdLine + " with client " + client);
			control.execute(".create " + cmdLine);
			control.waitForEvent();
			DebugProcessInfo pi = procInfo.getNow(null);
			assertNotNull(pi);
			control.execute("g");
			control.waitForEvent();
			DebugThreadInfo ti = threadInfo.getNow(null);
			assertNotNull(ti);
		}
	
		public void kill() {
			Msg.debug(this, "Killing " + cmdLine);
			control.execute(".kill");
			control.waitForEvent();
			Integer exitCode = procExit.getNow(null);
			client.setOutputCallbacks(null);
			assertNotNull(exitCode);
		}
	
		public List<String> execCapture(String command) {
			try {
				outputCapture = new StringBuilder();
				control.execute(command);
				return Arrays.asList(outputCapture.toString().split("\n"));
			}
			finally {
				outputCapture = null;
			}
		}
	
		@Override
		public void close() {
			if (procInfo.isDone() && !procExit.isDone()) {
				kill();
			}
		}
	}
	
	@Test
	public void testDescribeVector256Register() {
		try (ProcMaker maker = new ProcMaker("notepad")) {
			maker.start();
	
			DebugRegisters regs = client.getRegisters();
			int index = regs.getIndexByName("ymm0");
			DebugRegisterDescription desc = regs.getDescription(index);
			Msg.debug(this, "desc=" + desc);
			// This seems wrong, but I'm curious about the value
			assertEquals(DebugValueType.VECTOR128, desc.type);
	
			DebugValue value = regs.getValue(index);
			Msg.debug(this, "value=" + value);
		}
	}
	
	@Test
	public void testGetSingleRegister() {
		try (ProcMaker maker = new ProcMaker("notepad")) {
			maker.start();
	
			List<String> out = maker.execCapture("r");
			String expected =
				out.stream().filter(s -> s.startsWith("rax")).findAny().get().split("\\s+")[0];
	
			DebugRegisters regs = client.getRegisters();
			DebugInt64Value raxVal = (DebugInt64Value) regs.getValueByName("rax");
	
			String actual = String.format("rax=%016x", raxVal.longValue());
			assertEquals(expected, actual);
		}
	}
	
	@Test
	public void testGetRegisters() {
		try (ProcMaker maker = new ProcMaker("notepad")) {
			maker.start();
	
			List<String> out = maker.execCapture("r");
			String expected = out.stream().filter(s -> s.startsWith("rax")).findAny().get();
	
			DebugRegisters regs = client.getRegisters();
			List<Integer> indices = new ArrayList<>();
			int raxIdx = regs.getIndexByName("rax");
			int rbxIdx = regs.getIndexByName("rbx");
			int rcxIdx = regs.getIndexByName("rcx");
			indices.add(raxIdx);
			indices.add(rbxIdx);
			indices.add(rcxIdx);
			Map<Integer, DebugValue> values =
				regs.getValues(DebugRegisterSource.DEBUG_REGSRC_DEBUGGEE, indices);
	
			String actual = String.format("rax=%016x rbx=%016x rcx=%016x",
				((DebugInt64Value) values.get(raxIdx)).longValue(),
				((DebugInt64Value) values.get(rbxIdx)).longValue(),
				((DebugInt64Value) values.get(rcxIdx)).longValue());
			assertEquals(expected, actual);
		}
	}
	
	@Test
	public void testSetSingleRegister() {
		try (ProcMaker maker = new ProcMaker("notepad")) {
			maker.start();
	
			DebugRegisters regs = client.getRegisters();
			regs.setValueByName("rax", new DebugInt64Value(0x0102030405060708L));
	
			List<String> out = maker.execCapture("r");
			String actual =
				out.stream().filter(s -> s.startsWith("rax")).findAny().get().split("\\s+")[0];
			assertEquals("rax=0102030405060708", actual);
		}
	}
	
	@Test
	public void testSetRegisters() {
		try (ProcMaker maker = new ProcMaker("notepad")) {
			maker.start();
	
			DebugRegisters regs = client.getRegisters();
			// Purposefully choosing non-linked variant.
			// Want to know that order does not make a difference.
			Map<Integer, DebugValue> values = new HashMap<>();
			values.put(regs.getIndexByName("rax"), new DebugInt64Value(0x0102030405060708L));
			values.put(regs.getIndexByName("rbx"), new DebugInt64Value(0x1122334455667788L));
			values.put(regs.getIndexByName("rcx"), new DebugInt64Value(0x8877665544332211L));
			regs.setValues(DebugRegisterSource.DEBUG_REGSRC_DEBUGGEE, values);
	
			List<String> out = maker.execCapture("r");
			String actual = out.stream().filter(s -> s.startsWith("rax")).findAny().get();
			assertEquals("rax=0102030405060708 rbx=1122334455667788 rcx=8877665544332211", actual);
		}
	}
	
	@Test
	public void testQueryVirtual() {
		// Also, an experiment to figure out how it works
		try (ProcMaker maker = new ProcMaker("notepad")) {
			maker.start();
	
			List<DebugMemoryBasicInformation> collected1 = new ArrayList<>();
			try {
				long last = 0;
				long offset = 0;
				do {
					System.out.print(Long.toHexString(offset) + ": ");
					DebugMemoryBasicInformation info = client.getDataSpaces().queryVirtual(offset);
					//System.out.println(info);
					System.out.println(Long.toHexString(info.baseAddress) + "-" +
						Long.toHexString(info.regionSize) + ": " + info.state);
					if (info.baseAddress != offset) {
						System.out.println("  !!!");
					}
					collected1.add(info);
					last = offset;
					offset += info.regionSize;
				}
				while (Long.compareUnsigned(last, offset) < 0);
			}
			catch (COMException e) {
				if (!e.getMessage().contains("HRESULT: 80004002")) {
					throw e;
				}
			}
	
			List<DebugMemoryBasicInformation> collected2 = new ArrayList<>();
			for (DebugMemoryBasicInformation info : client.getDataSpaces().iterateVirtual(0)) {
				collected2.add(info);
			}
	
			assertTrue(collected1.size() > 0);
			assertEquals(collected1, collected2);
	
			// For comparison
			client.getControl().execute("!address");
		}
	}
	
	@Test
	public void testModules() {
		try (ProcMaker maker = new ProcMaker("notepad")) {
			maker.start();
	
			for (DebugModule mod : client.getSymbols().iterateModules(0)) {
				System.out.println(mod.getIndex() + ": " + Long.toHexString(mod.getBase()) + ": " +
					mod.getName(DebugModuleName.MODULE));
				System.out.println("  Img: " + mod.getName(DebugModuleName.IMAGE));
				System.out.println("  Load: " + mod.getName(DebugModuleName.LOADED_IMAGE));
			}
		}
	}
	
	@Test(expected = COMException.class)
	public void testModuleOutOfBounds() {
		try (ProcMaker maker = new ProcMaker("notepad")) {
			maker.start();
	
			DebugModule umod = client.getSymbols()
					.getModuleByIndex(
						client.getSymbols().getNumberLoadedModules() + 1);
			System.out.println(umod.getBase());
		}
	}
	
	@Test
	public void testQueryVirtualWithModule() {
		try (ProcMaker maker = new ProcMaker("notepad")) {
			maker.start();
	
			for (DebugMemoryBasicInformation info : client.getDataSpaces().iterateVirtual(0)) {
				if (info.state != PageState.FREE) {
					DebugModule mod = null;
					String name = "[NONE]";
					try {
						mod = client.getSymbols().getModuleByOffset(info.baseAddress, 0);
						name = mod.getName(DebugModuleName.IMAGE);
					}
					catch (COMException e) {
						name = "[ERR:" + e + "]";
					}
					System.out.println(String.format("%016x", info.baseAddress) + ":" +
						Long.toHexString(info.regionSize) + ":" + info.state + " from " + name +
						" " + info.type + info.protect);
				}
			}
		}
	}
	
	@Test
	public void testSymbols() {
		try (ProcMaker maker = new ProcMaker("notepad")) {
			maker.start();
	
			Set<DebugSymbolName> symbols = new LinkedHashSet<>();
			Set<String> modules = new LinkedHashSet<>();
			for (DebugSymbolName sym : client.getSymbols().iterateSymbolMatches("*")) {
				String[] parts = sym.name.split("!");
				symbols.add(sym);
				modules.add(parts[0]);
			}
			System.out.println("Total Symbols: " + symbols.size());
			System.out.println("Total Modules (by symbol name): " + modules.size());
	
			// These make assumptions that could be broken later.
			// It used to expect at least 10 modules (devised when testing on Win7). Now it's 5!
			assertTrue("Fewer than 1000 symbols: " + symbols.size(), symbols.size() > 1000);
			assertTrue("Fewer than 3 modules: " + modules.size(), modules.size() > 3);
		}
	}
	
	@Test
	public void testSymbolInfo() {
		try (ProcMaker maker = new ProcMaker("notepad")) {
			maker.start();
	
			int count = 0;
			for (DebugSymbolId symid : client.getSymbols().getSymbolIdsByName("ntdll!*")) {
				//System.out.println(symid);
				DebugSymbolEntry syment = client.getSymbols().getSymbolEntry(symid);
				if (syment.typeId != 0) {
					System.out.println("  " + syment);
				}
				count++;
			}
	
			assertTrue(count > 10);
		}
	}
	
	@Test
	public void testReadMemory() throws FileNotFoundException, IOException {
		try (ProcMaker maker = new ProcMaker("notepad")) {
			maker.start();
	
			control.execute(".server tcp:port=54321");
			int len = 256;
	
			DebugModule notepadModule = client.getSymbols().getModuleByModuleName("notepad", 0);
			System.out.println("Base: " + Long.toHexString(notepadModule.getBase()));
			ByteBuffer data = ByteBuffer.allocate(len);
			client.getDataSpaces().readVirtual(notepadModule.getBase(), data, data.remaining());
			System.out.println(NumericUtilities.convertBytesToString(data.array()));
	
			// TODO: Avoid hardcoding path to notepad
			try (FileInputStream fis = new FileInputStream("C:\\Windows\\notepad.exe")) {
				byte[] fromFile = new byte[len];
				fis.read(fromFile);
				// TODO: Note sure why, but this seems to be the case after it's loaded
				//ByteBuffer toWriteBase = ByteBuffer.wrap(fromFile).order(ByteOrder.LITTLE_ENDIAN);
				//toWriteBase.putLong(280, notepadModule.getBase());
				System.out.println(NumericUtilities.convertBytesToString(fromFile));
				assertArrayEquals(fromFile, data.array());
			}
	
			data.clear();
			data.putInt(0x12345678);
			client.getDataSpaces().readVirtual(notepadModule.getBase(), data, data.remaining());
			data.flip();
	
			assertEquals(0x12345678, data.getInt());
		}
	}
	
	@Test
	public void testWriteMemory() {
		try (ProcMaker maker = new ProcMaker("notepad")) {
			maker.start();
	
			// TODO: How to write to protected memory?
			// Debugger should be able to modify program code.
			DebugMemoryBasicInformation writable = null;
			space: for (DebugMemoryBasicInformation info : client.getDataSpaces()
					.iterateVirtual(
						0)) {
				for (PageProtection prot : info.protect) {
					if (prot.isWrite()) {
						writable = info;
						break space;
					}
				}
			}
			if (writable == null) {
				throw new AssertionError("No writable pages?");
			}
			System.out.println("writable: " + writable);
			ByteBuffer toWrite = ByteBuffer.allocate(10);
			toWrite.putInt(0x12345678);
			toWrite.putInt(0x89abcdef);
			toWrite.putShort((short) 0x5555);
			toWrite.flip();
			client.getDataSpaces().writeVirtual(writable.baseAddress, toWrite, toWrite.remaining());
	
			ByteBuffer toRead = ByteBuffer.allocate(10);
			client.getDataSpaces().readVirtual(writable.baseAddress, toRead, toRead.remaining());
	
			assertArrayEquals(toWrite.array(), toRead.array());
		}
	}
	
	@Test
	public void testBreakpoints() {
		try (ProcMaker maker = new ProcMaker("notepad")) {
			maker.start();
	
			DebugBreakpoint bpt = control.addBreakpoint(BreakType.CODE);
			System.out.println("Breakpoint id: " + bpt.getId());
			System.out.println("Flags: " + bpt.getFlags());
			DebugBreakpoint bpt2 = control.getBreakpointById(bpt.getId());
			assertEquals(bpt, bpt2);
		}
	}
	
	@Test
	public void testFreezeUnfreeze() {
		try (ProcMaker maker = new ProcMaker("notepad")) {
			maker.start();
	
			// Trying to see if any events will help me track frozen threads
			System.out.println("****Freezing");
			control.execute("~0 f");
			System.out.println("****Unfreezing");
			control.execute("~0 u");
			System.out.println("****Done");
			// Well, that result stinks.
			// There is no event to tell me about frozenness
		}
	}
	
	@Test
	@Ignore("I can't find a reliable means to detect the last thread. " +
		"There's supposed to be an initial break, but it is rarely reported. " +
		"I thought about toolhelp, but that presumes local live debugging.")
	public void testMultiThreadAttach() throws Exception {
		// I need to see how to attach to multi-threaded processes. There must be some event
		// or condition to indicate when all threads have been discovered.
		String specimen =
			Application.getOSFile("sctldbgeng", "expCreateThreadSpin.exe").getCanonicalPath();
		client.setOutputCallbacks(new DebugOutputCallbacks() {
			@Override
			public void output(int mask, String text) {
				System.out.print(text);
				System.out.flush();
			}
		});
		client.setEventCallbacks(new DebugEventCallbacksAdapter() {
			@Override
			public DebugStatus breakpoint(DebugBreakpoint bp) {
				control.outln("*** Breakpoint: " + bp);
				return DebugStatus.BREAK;
			}
	
			@Override
			public DebugStatus exception(DebugExceptionRecord64 exception, boolean firstChance) {
				control.outln("*** Exception: " + exception + "," + firstChance);
				return DebugStatus.BREAK;
			}
	
			@Override
			public DebugStatus createThread(DebugThreadInfo debugThreadInfo) {
				control.outln("*** CreateThread: " + debugThreadInfo);
				System.out.println("Threads: " + client.getSystemObjects().getThreads());
				return DebugStatus.BREAK;
			}
	
			@Override
			public DebugStatus createProcess(DebugProcessInfo debugProcessInfo) {
				control.outln("*** CreateProcess: " + debugProcessInfo);
				System.out.println("Threads: " + client.getSystemObjects().getThreads());
				return DebugStatus.BREAK;
			}
	
			@Override
			public DebugStatus exitThread(int exitCode) {
				control.outln("*** ExitThread: code=" + exitCode + ", " +
					client.getSystemObjects().getEventThread());
				System.out.println("Threads: " + client.getSystemObjects().getThreads());
				return DebugStatus.BREAK;
			}
	
			@Override
			public DebugStatus exitProcess(int exitCode) {
				control.outln("*** ExitProcess: code=" + exitCode + ", " +
					client.getSystemObjects().getEventProcess());
				System.out.println("Threads: " + client.getSystemObjects().getThreads());
				return DebugStatus.BREAK;
			}
	
			@Override
			public DebugStatus changeEngineState(BitmaskSet<ChangeEngineState> flags,
					long argument) {
				if (flags.contains(ChangeEngineState.EXECUTION_STATUS)) {
					control.outln(
						"*** ExecutionStatus: " + control.getExecutionStatus());
				}
				return DebugStatus.NO_CHANGE;
			}
		});
		try (DummyProc proc = new DummyProc(specimen)) {
			System.out.println("Started " + specimen + " with PID=" + proc.pid);
			Thread.sleep(1000);
			System.out.println("Attaching...");
			client.attachProcess(client.getLocalServer(), proc.pid, BitmaskSet.of());
			if (true) {
				for (int i = 0; i < 10; i++) {
					System.out.println("WAIT " + i + "...");
					control.waitForEvent(100);
					System.out.println("STATUS: " + control.getExecutionStatus());
					System.out.println("DONE " + i);
					// control.execute("~*");
				}
			}
		}
		finally {
			client.setEventCallbacks(null);
		}
	}
	
	protected static class BreakAllCallbacks extends DebugEventCallbacksAdapter {
		protected final DebugControl control;
		protected final DebugRegisters regs;
		protected final DebugSystemObjects objs;
	
		volatile protected long currentThread = 0;
		volatile protected DebugProcessInfo createdProc = null;
		volatile protected String lastReg = null;
	
		public BreakAllCallbacks(DebugClient client) {
			this.control = client.getControl();
			this.regs = client.getRegisters();
			this.objs = client.getSystemObjects();
		}
	
		@Override
		public DebugStatus breakpoint(DebugBreakpoint bp) {
			control.outln("*** Breakpoint: " + bp);
			return DebugStatus.BREAK;
		}
	
		@Override
		public DebugStatus changeDebuggeeState(BitmaskSet<ChangeDebuggeeState> flags,
				long argument) {
			control.outln("*** ChangeDebuggeeState: " + flags + "," + argument);
			if (flags.contains(ChangeDebuggeeState.REGISTERS) &&
				!flags.contains(ChangeDebuggeeState.REFRESH)) {
				DebugRegisterDescription description = regs.getDescription((int) argument);
				control.outln("   Process: " + objs.getEventProcess());
				control.outln("   Thread: " + Long.toHexString(objs.getCurrentThreadSystemId()));
				control.outln("   Reg: " + description);
				control.outln("   Val: " + NumericUtilities
						.convertBytesToString(regs.getValue((int) argument).encodeAsBytes(), ""));
				lastReg = description.name;
			}
			return DebugStatus.BREAK;
		}
	
		@Override
		public DebugStatus changeEngineState(BitmaskSet<ChangeEngineState> flags, long argument) {
			control.outln("*** ChangeEngineState: " + flags + "," + Long.toHexString(argument));
			if (flags.contains(ChangeEngineState.CURRENT_THREAD)) {
				currentThread = argument;
			}
			return DebugStatus.BREAK;
		}
	
		@Override
		public DebugStatus changeSymbolState(BitmaskSet<ChangeSymbolState> flags, long argument) {
			control.outln("*** ChangeSymbolState: " + flags + "," + argument);
			return DebugStatus.BREAK;
		}
	
		@Override
		public DebugStatus createProcess(DebugProcessInfo debugProcessInfo) {
			control.outln("*** CreateProcess: " + debugProcessInfo);
			return DebugStatus.BREAK;
		}
	
		@Override
		public DebugStatus createThread(DebugThreadInfo debugThreadInfo) {
			control.outln("*** CreateThread: " + debugThreadInfo);
			return DebugStatus.BREAK;
		}
	
		@Override
		public DebugStatus exception(DebugExceptionRecord64 exception, boolean firstChance) {
			control.outln("*** Exception: " + exception + "," + firstChance);
			return DebugStatus.BREAK;
		}
	
		@Override
		public DebugStatus exitProcess(int exitCode) {
			control.outln("*** ExitProcess: code=" + exitCode);
			return DebugStatus.BREAK;
		}
	
		@Override
		public DebugStatus exitThread(int exitCode) {
			control.outln("*** ExitThread: code=" + exitCode);
			return DebugStatus.BREAK;
		}
	
		@Override
		public DebugStatus loadModule(DebugModuleInfo debugModuleInfo) {
			control.outln("*** LoadModule: " + debugModuleInfo);
			return DebugStatus.BREAK;
		}
	
		@Override
		public DebugStatus sessionStatus(SessionStatus status) {
			control.outln("*** SessionStatus: " + status);
			return DebugStatus.BREAK;
		}
	
		@Override
		public DebugStatus systemError(int error, int level) {
			control.outln("*** SystemError: " + error + "," + level);
			return DebugStatus.BREAK;
		}
	
		@Override
		public DebugStatus unloadModule(String imageBaseName, long baseOffset) {
			control.outln("*** UnloadModule: " + imageBaseName + "," + baseOffset);
			return DebugStatus.BREAK;
		}
	}
	
	protected static class ConsoleOutputCallbacks implements DebugOutputCallbacks {
		@Override
		public void output(int mask, String text) {
			System.out.print(text);
			System.out.flush();
		}
	}
	
	@Test
	public void testAttachLaunch() throws Exception {
		final String specimenX = "C:\\windows\\write.exe";
		final String specimenA = "C:\\windows\\notepad.exe";
		final String specimenC = "C:\\windows\\system32\\win32calc.exe";
	
		try (DummyProc proc = new DummyProc(specimenX)) {
			client.setOutputCallbacks(new ConsoleOutputCallbacks());
			BreakAllCallbacks cb = new BreakAllCallbacks(client);
			client.setEventCallbacks(cb);
			System.out.println("Started " + specimenA + " with PID=" + proc.pid);
			Thread.sleep(1000);
	
			//System.out.println("Attaching...");
			//client.attachProcess(client.getLocalServer(), proc.pid, BitmaskSet.of());
			client.createProcess(client.getLocalServer(), specimenA,
				BitmaskSet.of(DebugCreateFlags.DEBUG_PROCESS));
	
			cb.lastReg = null;
			//while (cb.lastReg == null) {
			while (true) {
				if (cb.currentThread != 0) {
					client.getControl().execute("~* r rip");
				}
				client.getControl().waitForEvent();
			}
	/*
			final Map<DebugThreadId, String> ripValsPreLaunch = new HashMap<>();
			for (DebugThreadId tid : client.getSystemObjects().getThreads()) {
				client.getSystemObjects().setCurrentThreadId(tid);
				byte[] val = client.getRegisters().getValue(16).encodeAsBytes();
				ripValsPreLaunch.put(tid, new BigInteger(1, val).toString(16));
			}
	
			System.out.println("Creating...");
			client.createProcess(client.getLocalServer(), specimenC,
				BitmaskSet.of(DebugCreateFlags.DEBUG_PROCESS));
	
			cb.lastReg = null;
			while (cb.lastReg == null) {
				System.err.println(client.getSystemObjects().getCurrentProcessId());
				client.getControl().waitForEvent();
				client.getSystemObjects().setCurrentProcessId(new DebugProcessId(1));
				DebugEventInformation info = client.getControl().getLastEventInformation();
				System.err.println(info.getProcessId());
			}
	
			client.getSystemObjects().setCurrentProcessId(new DebugProcessId(0));
			final Map<DebugThreadId, String> ripValsPostLaunch = new HashMap<>();
			for (DebugThreadId tid : client.getSystemObjects().getThreads()) {
				client.getSystemObjects().setCurrentThreadId(tid);
				byte[] val = client.getRegisters().getValue(16).encodeAsBytes();
				ripValsPostLaunch.put(tid, new BigInteger(1, val).toString(16));
			}
	
			assertEquals(ripValsPreLaunch, ripValsPostLaunch);
			//
		}
		finally {
			client.setOutputCallbacks(null);
			client.setEventCallbacks(null);
		}
	}
	*/
}
