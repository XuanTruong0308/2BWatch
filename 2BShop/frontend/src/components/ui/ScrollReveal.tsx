import { useEffect, useRef, useState, ReactNode } from "react";

export interface ScrollRevealProps {
  children: ReactNode;
  animation?: "fade-up" | "fade-down" | "fade-in" | "slide-left" | "slide-right" | "zoom-in";
  delay?: number;
  duration?: number;
  threshold?: number;
  className?: string;
  style?: React.CSSProperties;
}

export function ScrollReveal({
  children,
  animation = "fade-up",
  delay = 0,
  duration = 800,
  threshold = 0.1,
  className = "",
  style,
}: ScrollRevealProps) {
  const [isVisible, setIsVisible] = useState(false);
  const domRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    let hasIntersected = false;

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting && !hasIntersected) {
          hasIntersected = true;
          // Add a small delay for smoother progressive rendering
          setTimeout(() => setIsVisible(true), 50);
          if (domRef.current) observer.unobserve(domRef.current);
        }
      },
      {
        threshold,
        rootMargin: "0px 0px -50px 0px",
      }
    );

    if (domRef.current) {
      observer.observe(domRef.current);
    }

    return () => observer.disconnect();
  }, [threshold]);

  const combinedStyle: React.CSSProperties = {
    ...style,
    transitionDuration: `${duration}ms`,
    transitionDelay: `${delay}ms`,
  };

  return (
    <div
      ref={domRef}
      className={`reveal-element reveal-${animation} ${isVisible ? "is-revealed" : ""} ${className}`}
      style={combinedStyle}
    >
      {children}
    </div>
  );
}
