require 'stringio'
require 'irb'
require 'irb/completion'

class EclipseConsoleInputMethod
  # echo the prompt and get a line of input.
  def gets
	$stdout.print 'irb:> '    
	$stdin.gets
  end

  def prompt=(x)
  end
end
#----------------------------

class EclipseConsoleIrb < IRB::Irb

  def initialize(ec_inputmethod)
    IRB.setup(__FILE__)
    IRB.conf[:VERBOSE] = false
    super(nil, ec_inputmethod)
  end

  def run
    IRB.conf[:MAIN_CONTEXT] = self.context
    eval_input
  end
end

#----------------------------

eiMethod = EclipseConsoleInputMethod.new
eIrb = EclipseConsoleIrb.new(eiMethod)
#eIrb = IRB::Irb.new(nil, eiMethod)

#eIrb.eval_input
eIrb.run






