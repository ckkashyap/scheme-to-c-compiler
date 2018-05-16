from parse import parse
import subprocess, os, shlex, sys

def prRed(prt): print("\033[91m {}\033[00m" .format(prt))
def prGreen(prt): print("\033[92m {}\033[00m" .format(prt))

wd = os.getcwd()
os.chdir("..")

print('Switched to {d}'.format(d=os.getcwd()))

def exec(command):
	with subprocess.Popen(shlex.split(command), stdout=subprocess.PIPE) as proc:
		print('Starting {command}...'.format(command=command), end='')
		sys.stdout.flush()
		proc.wait()
		r = proc.stdout.read()
		print("Done")
		return r.decode('ascii')

def remove(f):
		print('{c} '.format(c=f), end='')
		if os.path.isfile(f):
			print('exists, so removing')
			os.remove(f)
		else:
			print('does not exist')

cljGeneratedC = 'test/test.c'
cljGeneratedExe = 'test/clj-generated.exe'
cljGeneratedExeOutput = 'test/clj-generated.exe.out'
pyGeneratedC = 'test/python.c'
pyGeneratedExe = 'test/python-generated.exe'
pyGeneratedExeOutput = 'test/python-generated.exe.out'

remove(cljGeneratedC)
remove(cljGeneratedExe)
remove(cljGeneratedExeOutput)
remove(pyGeneratedC)
remove(pyGeneratedExe)
remove(pyGeneratedExeOutput)

exec('./run.sh test.clj')
exec('python3 python/drive.py')

exec('gcc {c} -o {e}'.format(c=cljGeneratedC, e=cljGeneratedExe))
exec('gcc {c} -o {e}'.format(c=pyGeneratedC, e=pyGeneratedExe))

cljOutput = exec(cljGeneratedExe)
pyOutput = exec(pyGeneratedExe)

print('Comparing CLJ outut and PY output ', end='')
with open(cljGeneratedExeOutput, 'w') as f:
    f.write(cljOutput)
with open(pyGeneratedExeOutput, 'w') as f:
    f.write(pyOutput)
if cljOutput == pyOutput:
	prGreen ('PASS')
else:
	prRed ('FAIL')
