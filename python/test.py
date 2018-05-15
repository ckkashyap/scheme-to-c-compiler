from parse import parse
import subprocess, os, shlex, sys

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
pyGeneratedC = 'test/python.c'
pyGeneratedExe = 'test/python-generated.exe'

remove(cljGeneratedC)
remove(cljGeneratedExe)
remove(pyGeneratedC)
remove(pyGeneratedExe)

exec('./run.sh test.clj')
exec('python3 python/drive.py')

exec('gcc {c} -o {e}'.format(c=cljGeneratedC, e=cljGeneratedExe))
exec('gcc {c} -o {e}'.format(c=pyGeneratedC, e=pyGeneratedExe))
